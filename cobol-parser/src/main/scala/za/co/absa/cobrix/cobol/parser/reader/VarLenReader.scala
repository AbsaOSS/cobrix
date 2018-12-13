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

package za.co.absa.cobrix.cobol.parser.reader

import za.co.absa.cobrix.cobol.parser.reader.iterator.VarLenIterator
import za.co.absa.cobrix.cobol.parser.stream.SimpleStream
import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}

class VarLenReader (copybookContents: String, data: SimpleStream, lengthFieldName: String, startOffset: Int = 0, endOffset: Int = 0) extends GenericReader {

  override def getCobolSchema: Copybook = CopybookParser.parseTree(copybookContents)

  override def getIterator: Iterator[Seq[Any]] = {
    new VarLenIterator(getCobolSchema, data, lengthFieldName, startOffset, endOffset)
  }

}
