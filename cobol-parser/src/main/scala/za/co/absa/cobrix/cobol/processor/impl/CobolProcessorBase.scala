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

package za.co.absa.cobrix.cobol.processor.impl

import za.co.absa.cobrix.cobol.processor.CobolProcessor
import za.co.absa.cobrix.cobol.reader.VarLenNestedReader
import za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordExtractor
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

/**
  * Implements common methods of direct EBCDIC to EBCDIC data processor implementations.
  *
  * The processing can be done from inside an RDD so this is why it is serializable.
  */
abstract class CobolProcessorBase extends CobolProcessor with Serializable {
  private[processor] def getRecordExtractor(readerParameters: ReaderParameters, copybookContents: String, inputStream: SimpleStream): RawRecordExtractor = {
    val dataStream = inputStream.copyStream()
    val headerStream = inputStream.copyStream()

    val reader = new VarLenNestedReader[Array[Any]](Seq(copybookContents), readerParameters, new ArrayOfAnyHandler)

    reader.recordExtractor(0, dataStream, headerStream) match {
      case Some(extractor) => extractor
      case None            =>
        throw new IllegalArgumentException(s"Cannot create a record extractor for the given reader parameters. " +
          "Please check the copybook and the reader parameters."
        )
    }
  }
}
