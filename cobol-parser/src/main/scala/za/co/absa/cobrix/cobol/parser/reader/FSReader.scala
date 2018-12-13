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

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.encoding.EBCDIC
import za.co.absa.cobrix.cobol.parser.reader.iterator.FSRecordIterator

/** The class reads COBOL/EBCDIC files from the filesystem. Returns a records iterator. */
class FSReader (copybookFileName: String, binaryDataFileName: String) extends GenericReader  {

  val copybook: Copybook = loadCopybokFromFS(copybookFileName)

  override def getCobolSchema: Copybook = copybook

  override def getIterator: Iterator[Seq[Any]] = new FSRecordIterator(getCobolSchema, binaryDataFileName)

  def loadCopybokFromFS(copybookFile: String): Copybook = {
    val copyBookContents = Files.readAllLines(Paths.get(copybookFile), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val schema = CopybookParser.parseTree(copyBookContents)
    schema
  }

}
