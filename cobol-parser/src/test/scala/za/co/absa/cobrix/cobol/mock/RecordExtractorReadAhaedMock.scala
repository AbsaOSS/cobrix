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

import za.co.absa.cobrix.cobol.reader.extractors.raw.{RawRecordContext, RawRecordExtractor}

class RecordExtractorReadAhaedMock(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  ctx.dataStream.next(2)

  override def offset: Long = ctx.dataStream.offset

  override def hasNext: Boolean = !ctx.dataStream.isEndOfStream

  override def next(): Array[Byte] = {
    val header = ctx.dataStream.next(2)
    if (header.length == 2) {
      ctx.dataStream.next(header.head + header(1) * 256)
    } else {
      Array.empty[Byte]
    }
  }
}
