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

package za.co.absa.cobrix.spark.cobol.source.fixtures

import java.io.{DataOutputStream, File, FileOutputStream}
import java.nio.charset.Charset
import java.nio.file.{Files, Path}

import org.apache.commons.io.{FileSystemUtils, FileUtils}

/**
  * This fixture adds ability for a unit test to create temporary files for using them in the tests.
  */
trait BinaryFileFixture {

  /**
    * Creates a temporary text file and returns the full path to it
    *
    * @param prefix  The prefix string to be used in generating the file's name;
    *                must be at least three characters long
    * @param suffix  The suffix string to be used in generating the file's name;
    *                may be <code>null</code>, in which case the suffix <code>".tmp"</code> will be used
    * @param charset A charset of the data in the temporaty text file
    * @param content A contents to put to the file
    * @return The full path to the temporary file
    */
  def withTempTextFile(prefix: String, suffix: String, charset: Charset, content: String)(f: String => Unit): Unit = {
    val tempFile = File.createTempFile(prefix, suffix)
    val ostream = new DataOutputStream(new FileOutputStream(tempFile))
    ostream.write(content.getBytes(charset))
    ostream.close()

    f(tempFile.getAbsolutePath)

    tempFile.delete
  }

  /**
    * Creates a temporary binary file and returns the full path to it
    *
    * @param prefix  The prefix string to be used in generating the file's name;
    *                must be at least three characters long
    * @param suffix  The suffix string to be used in generating the file's name;
    *                may be <code>null</code>, in which case the suffix <code>".tmp"</code> will be used
    * @param content A contents to put to the file
    * @return The full path to the temporary file
    */
  def withTempBinFile(prefix: String, suffix: String, content: Array[Byte])(f: String => Unit): Unit = {
    val tempFile = File.createTempFile(prefix, suffix)
    val ostream = new DataOutputStream(new FileOutputStream(tempFile))
    ostream.write(content)
    ostream.close()

    f(tempFile.getAbsolutePath)

    tempFile.delete
  }

  def withTempDirectory(prefix: String)(f: String => Unit): Unit = {
    val tmpPath = Files.createTempDirectory(prefix)
    val pathStr = tmpPath.toAbsolutePath.toString

    f(pathStr)

    FileUtils.deleteDirectory(new File(pathStr))
  }

  /**
    * Creates a temporary binary file and returns the full path to it.
    * The input data is specified as a hexadecimal string, e.g "CAFE"
    *
    * @param prefix  The prefix string to be used in generating the file's name;
    *                must be at least three characters long
    * @param suffix  The suffix string to be used in generating the file's name;
    *                may be <code>null</code>, in which case the suffix <code>".tmp"</code> will be used
    * @param content A contents to put to the file
    * @return The full path to the temporary file
    */
  def withTempHexBinFile(prefix: String, suffix: String, content: String)(f: String => Unit): Unit = {
    val tempFile = File.createTempFile(prefix, suffix)
    val ostream = new DataOutputStream(new FileOutputStream(tempFile))
    val binContent = hex2bytes(content)

    ostream.write(binContent)
    ostream.close()

    f(tempFile.getAbsolutePath)

    tempFile.delete
  }

  private def hex2bytes(hex: String): Array[Byte] = {
    val compactStr = hex.replaceAll("\\s", "")
    compactStr.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }
}

