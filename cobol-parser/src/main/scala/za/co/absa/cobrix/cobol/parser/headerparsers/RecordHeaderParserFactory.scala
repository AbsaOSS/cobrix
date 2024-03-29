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

package za.co.absa.cobrix.cobol.parser.headerparsers

import za.co.absa.cobrix.cobol.internal.Logging
import za.co.absa.cobrix.cobol.parser.common.Constants

object RecordHeaderParserFactory extends Logging {

  def createRecordHeaderParser(parserTypeOrClass: String,
                               recordLength: Int,
                               fileStartOffset: Int,
                               fileEndOffset: Int,
                               rdwAdjustment: Int): RecordHeaderParser = {
    val parserTypeLowerCase = parserTypeOrClass.toLowerCase

    parserTypeLowerCase match {
      case Constants.RhXcom => new RecordHeaderParserRDW(isBigEndian = false, fileStartOffset, fileEndOffset, rdwAdjustment)
      case Constants.RhRdw => new RecordHeaderParserRDW(isBigEndian = false, fileStartOffset, fileEndOffset, rdwAdjustment)
      case Constants.RhRdwBigEndian => new RecordHeaderParserRDW(isBigEndian = true, fileStartOffset, fileEndOffset, rdwAdjustment)
      case Constants.RhRdwLittleEndian => new RecordHeaderParserRDW(isBigEndian = false, fileStartOffset, fileEndOffset, rdwAdjustment)
      case Constants.RhRdwFixedLength => new RecordHeaderParserFixedLen(recordLength, fileStartOffset, fileEndOffset)
      case _ =>
        logger.info(s"Using custom record parser class '$parserTypeOrClass'...")
        Class.forName(parserTypeOrClass)
          .newInstance()
          .asInstanceOf[RecordHeaderParser]
    }
  }
}
