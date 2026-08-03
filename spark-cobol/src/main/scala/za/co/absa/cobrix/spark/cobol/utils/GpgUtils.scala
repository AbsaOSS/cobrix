/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.cobrix.spark.cobol.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp._
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory
import org.bouncycastle.openpgp.operator.jcajce.{JcaKeyFingerprintCalculator, JcePBESecretKeyDecryptorBuilder, JcePublicKeyDataDecryptorFactoryBuilder}

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.security.{Provider, Security}
import scala.collection.JavaConverters._

object GpgUtils {
  /**
    * The BouncyCastle security provider used for all PGP cryptographic operations.
    *
    * The provider instance is registered on demand and passed explicitly to the JCE builders so that the
    * code does not depend on the "BC" provider alias being present in the JVM security provider list.
    * This is required when the BouncyCastle classes are shaded/relocated.
    */
  private lazy val bcProvider: Provider = {
    Option(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)).getOrElse {
      val provider = new BouncyCastleProvider
      Security.addProvider(provider)
      provider
    }
  }

  /**
    * Decrypts a PGP encrypted stream using a secret key from the provided ASCII armored keychain.
    *
    * The method looks up the public key encrypted session keys contained in the input stream and matches them
    * against the secret keys available in the keychain. The first matching secret key is unlocked with the given
    * passphrase and used to decrypt the data. Compressed messages are transparently decompressed so that the
    * returned stream exposes the original, unencrypted content of the PGP literal data packet.
    *
    * The returned stream is lazily read from the input stream, so the input stream should stay open until the
    * decrypted data is fully consumed, and the returned stream should be closed by the caller.
    *
    * @param in            An input stream containing PGP encrypted data, either binary or ASCII armored.
    * @param keychainAscii An ASCII armored keychain containing the secret key that can decrypt the data.
    * @param passphrase    The passphrase protecting the secret key.
    * @return An input stream with the decrypted contents of the message.
    * @throws IllegalArgumentException if the input stream does not contain PGP encrypted data, if no secret key in
    *                                  the keychain matches the encrypted data, or if the decrypted message does not
    *                                  contain literal data.
    */
  def decryptStream(in: InputStream,
                    keychainAscii: String,
                    passphrase: Array[Char]): InputStream = {
    val secretKeyRings = readSecretKeyRings(keychainAscii)

    val encryptedDataList = findEncryptedDataList(PGPUtil.getDecoderStream(in))

    val publicKeyEncryptedData = encryptedDataList
      .getEncryptedDataObjects
      .asScala
      .collect { case data: PGPPublicKeyEncryptedData => data }
      .toSeq

    val (encryptedData, privateKey) = publicKeyEncryptedData
      .flatMap(data => findPrivateKey(secretKeyRings, data.getKeyID, passphrase).map(key => (data, key)))
      .headOption
      .getOrElse(throw new IllegalArgumentException("No secret key in the provided keychain matches the encrypted data."))

    val decryptorFactory = new JcePublicKeyDataDecryptorFactoryBuilder()
      .setProvider(bcProvider)
      .build(privateKey)

    getLiteralDataStream(encryptedData.getDataStream(decryptorFactory))
  }

  /**
    * Parses an ASCII armored PGP keychain into a collection of secret key rings.
    *
    * The keychain text is decoded from its UTF-8 representation and passed through a PGP decoder stream, so that both
    * ASCII armored and plain binary keychain contents are accepted. All secret key rings found in the keychain are
    * loaded eagerly, therefore the stream used for reading is closed before the collection is returned.
    *
    * @param keychainAscii An ASCII armored keychain containing one or more secret key rings.
    * @return A collection of all secret key rings contained in the keychain.
    * @throws java.io.IOException                   if the keychain cannot be read or is malformed.
    * @throws org.bouncycastle.openpgp.PGPException if the keychain does not contain valid secret key ring data.
    */
  private def readSecretKeyRings(keychainAscii: String): PGPSecretKeyRingCollection = {
    val keyIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(keychainAscii.getBytes(StandardCharsets.UTF_8)))
    try {
      new PGPSecretKeyRingCollection(keyIn, new JcaKeyFingerprintCalculator)
    } finally {
      keyIn.close()
    }
  }

  /**
    * Locates the list of public key encrypted session keys in a PGP message stream.
    *
    * The objects of the PGP message are traversed in order until an encrypted data list is encountered, so that leading
    * packets, such as marker packets, are skipped. The stream may contain either binary or already de-armored PGP data.
    *
    * The returned data list is read lazily from the given stream, therefore the stream must remain open for as long as
    * the encrypted data is being processed.
    *
    * @param inputStream A stream positioned at the beginning of a PGP message.
    * @return The encrypted data list holding the encrypted session keys and the encrypted payload of the message.
    * @throws IllegalArgumentException if the stream does not contain PGP encrypted data.
    */
  private def findEncryptedDataList(inputStream: InputStream): PGPEncryptedDataList = {
    val factory = new JcaPGPObjectFactory(inputStream)

    Iterator.continually(factory.nextObject())
      .takeWhile(_ != null)
      .collectFirst { case dataList: PGPEncryptedDataList => dataList }
      .getOrElse(throw new IllegalArgumentException("The input stream does not contain PGP encrypted data."))
  }

  /**
    * Extracts the literal data stream from a decrypted PGP message stream.
    *
    * The objects of the PGP message are traversed until a literal data packet is found. Compressed data packets are
    * transparently unwrapped, so that nested compressed content is also inspected, while any other packet types, such
    * as signature or marker packets, are skipped.
    *
    * The returned stream is read lazily from the given stream, therefore the given stream must remain open until the
    * literal data is fully consumed.
    *
    * @param clearStream A stream containing the decrypted, but still PGP structured, message data.
    * @return An input stream exposing the contents of the literal data packet of the message.
    * @throws IllegalArgumentException if the message does not contain a literal data packet.
    */
  private def getLiteralDataStream(clearStream: InputStream): InputStream = {
    var factory = new JcaPGPObjectFactory(clearStream)
    var message = factory.nextObject()
    var literalStream: Option[InputStream] = None

    while (message != null && literalStream.isEmpty) {
      message match {
        case compressedData: PGPCompressedData =>
          factory = new JcaPGPObjectFactory(compressedData.getDataStream)
          message = factory.nextObject()
        case literalData: PGPLiteralData       =>
          literalStream = Option(literalData.getInputStream)
        case _                                 =>
          message = factory.nextObject()
      }
    }

    literalStream.getOrElse(throw new IllegalArgumentException("The decrypted PGP message does not contain literal data."))
  }


  /**
    * Retrieves and unlocks the private key with the given key identifier from a collection of secret key rings.
    *
    * The secret key rings are searched for a secret key matching the requested key identifier. If such a key exists,
    * it is decrypted with the given passphrase using the configured security provider, yielding the usable private key.
    * If no secret key with the given identifier is present in the collection, no attempt to decrypt anything is made.
    *
    * @param secretKeyRings A collection of secret key rings to search for the requested key.
    * @param keyId          The identifier of the secret key to look for.
    * @param passphrase     The passphrase protecting the secret key.
    * @return The unlocked private key, or `None` if the collection does not contain a key with the given identifier.
    * @throws org.bouncycastle.openpgp.PGPException if the secret key cannot be unlocked with the given passphrase.
    */
  private def findPrivateKey(secretKeyRings: PGPSecretKeyRingCollection,
                             keyId: Long,
                             passphrase: Array[Char]): Option[PGPPrivateKey] = {
    Option(secretKeyRings.getSecretKey(keyId)).map { secretKey =>
      val decryptor = new JcePBESecretKeyDecryptorBuilder().setProvider(bcProvider).build(passphrase)
      secretKey.extractPrivateKey(decryptor)
    }
  }
}
