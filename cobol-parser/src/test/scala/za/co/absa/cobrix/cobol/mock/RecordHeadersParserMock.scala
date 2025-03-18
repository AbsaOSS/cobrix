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

package za.co.absa.cobrix.cobol.mock

import za.co.absa.cobrix.cobol.parser.headerparsers.{RecordHeaderParser, RecordMetadata}

class RecordHeadersParserMock extends RecordHeaderParser {
  var isHeaderDefinedInCopybook: Boolean = false

  override def getHeaderLength: Int = 2

  override def getRecordMetadata(header: Array[Byte], fileOffset: Long, maxOffset: Long, fileSize: Long, recordNum: Long): RecordMetadata = {
    if (header.length == 2) {
      RecordMetadata((header.head + 256) % 256 + ((header(1) + 256) % 256)* 256, isValid = true)
    } else {
      RecordMetadata(0, isValid = false)
    }
  }
}
