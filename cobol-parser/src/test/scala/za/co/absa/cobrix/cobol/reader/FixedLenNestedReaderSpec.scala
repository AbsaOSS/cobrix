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

package za.co.absa.cobrix.cobol.reader

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePageCommon
import za.co.absa.cobrix.cobol.parser.policies.{FillerNamingPolicy, StringTrimmingPolicy}
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy

class FixedLenNestedReaderSpec extends AnyWordSpec {
  private val fixedLengthDataExample = Array(
    0xF1, 0xF2, 0xF3, 0xF4, // record 0
    0xF5, 0xF6, 0xF7, 0xF8  // record 1
  ).map(_.toByte)

  val copybookContents: String =
    """       01  RECORD.
      |          05  A PIC X(2).
      |          05  B PIC X(2).
      |      |""".stripMargin

  val copybookContents1: String =
    """       01  RECORD1.
      |          05  A PIC X(2).
      |      |""".stripMargin

  val copybookContents2: String =
    """       01  RECORD2.
      |          05  B PIC X(2).
      |      |""".stripMargin


  "getCobolSchema()" should {
    "return a schema for a single copybook" in {
      val reader = getUseCase(Seq(copybookContents))

      val schema = reader.getCobolSchema

      assert(schema.getCobolSchema.getCobolSchema.name == "_ROOT_")
      assert(schema.getCobolSchema.getCobolSchema.children.head.name == "RECORD")
    }

    "return a schema for multiple copybooks" in {
      val reader = getUseCase(Seq(copybookContents1, copybookContents2))

      val schema = reader.getCobolSchema

      assert(schema.getCobolSchema.getCobolSchema.name == "_ROOT_")
      assert(schema.getCobolSchema.getCobolSchema.children.head.name == "RECORD1")
      assert(schema.getCobolSchema.getCobolSchema.children(1).name == "RECORD2")
      assert(schema.getCobolSchema.getCobolSchema.children(1).redefines.contains("RECORD1"))
    }
  }

  "getRecordSize()" should {
    "return a record size for a single copybook" in {
      val reader = getUseCase(Seq(copybookContents))

      val recordSize = reader.getRecordSize

      assert(recordSize == 4)
    }

    "return a record size for multiple copybooks" in {
      val reader = getUseCase(Seq(copybookContents1, copybookContents2))

      val recordSize = reader.getRecordSize

      assert(recordSize == 2)
    }

    "return a record size for files with start offset" in {
      val reader = getUseCase(Seq(copybookContents1, copybookContents2), startOffset = 10)

      val recordSize = reader.getRecordSize

      assert(recordSize == 12)
    }
  }

  "getRecordIterator()" should {
    "return an iterator for single record data" in {
      val reader = getUseCase(Seq(copybookContents))

      val it = reader.getRecordIterator(fixedLengthDataExample)

      assert(it.hasNext)
      assert(it.next() == Seq("12", "34"))
      assert(!it.hasNext)
    }

    "return an iterator for multiple record data" in {
      val reader = getUseCase(Seq(copybookContents), recordFormat = RecordFormat.VariableLength)

      val it = reader.getRecordIterator(fixedLengthDataExample)

      assert(it.hasNext)
      assert(it.next() == Seq("12", "34"))
      assert(it.hasNext)
      assert(it.next() == Seq("56", "78"))
      assert(!it.hasNext)
    }
  }

  "checkBinaryDataValidity()" should {
    "pass the check if everything is okay" in {
      val reader = getUseCase(Seq(copybookContents))

      reader.checkBinaryDataValidity(fixedLengthDataExample)
    }

    "catch the record length is okay" in {
      val reader = getUseCase(Seq(copybookContents), recordLength = Some(4))

      reader.checkBinaryDataValidity(fixedLengthDataExample)
    }

    "catch negative start offset" in {
      val reader = getUseCase(Seq(copybookContents), startOffset = -1)

      assertThrows[IllegalArgumentException] {
        reader.checkBinaryDataValidity(fixedLengthDataExample)
      }
    }

    "catch negative end offset" in {
      val reader = getUseCase(Seq(copybookContents), endOffset = -1)

      assertThrows[IllegalArgumentException] {
        reader.checkBinaryDataValidity(fixedLengthDataExample)
      }
    }

    "catch negative record length" in {
      val reader = getUseCase(Seq(copybookContents), recordLength = Some(-1))

      assertThrows[IllegalArgumentException] {
        reader.checkBinaryDataValidity(fixedLengthDataExample)
      }
    }

    "catch binary data too small" in {
      val data = Array(0xF1, 0xF2, 0xF3).map(_.toByte)

      val reader = getUseCase(Seq(copybookContents))

      assertThrows[IllegalArgumentException] {
        reader.checkBinaryDataValidity(data)
      }
    }

    "catch record size does not divide data size" in {
      val data = Array(
        0xF1, 0xF2, 0xF3, 0xF4,      // record 0
        0xF5, 0xF6, 0xF7, 0xF8, 0xF9 // record 1
      ).map(_.toByte)

      val reader = getUseCase(Seq(copybookContents))

      assertThrows[IllegalArgumentException] {
        reader.checkBinaryDataValidity(data)
      }
    }
  }

  def getUseCase(copybooks: Seq[String],
                 recordFormat: RecordFormat = RecordFormat.FixedLength,
                 startOffset: Int = 0,
                 endOffset: Int = 0,
                 recordLength: Option[Int] = None,
                ): FixedLenNestedReader[scala.Array[Any]] = {
    val readerProperties = za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters(
      recordFormat = recordFormat,
      recordLength = recordLength
    )

    val reader = new FixedLenNestedReader[scala.Array[Any]](
      copybooks,
      isEbcdic = true,
      ebcdicCodePage = new CodePageCommon,
      floatingPointFormat = FloatingPointFormat.IEEE754,
      startOffset = startOffset,
      endOffset = endOffset,
      schemaRetentionPolicy = SchemaRetentionPolicy.CollapseRoot,
      stringTrimmingPolicy = StringTrimmingPolicy.TrimBoth,
      dropGroupFillers = false,
      dropValueFillers = false,
      fillerNamingPolicy = FillerNamingPolicy.SequenceNumbers,
      nonTerminals = Nil,
      occursMappings = Map.empty,
      readerProperties = readerProperties,
      handler = new SimpleRecordHandler)

   reader
  }
}
