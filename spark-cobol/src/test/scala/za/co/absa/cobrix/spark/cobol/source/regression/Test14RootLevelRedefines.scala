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

package za.co.absa.cobrix.spark.cobol.source.regression

import org.scalatest.WordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

/**
  * This suite validates issues observed when root level fields are redefined, including the case of several copybooks merging.
  */
class Test14RootLevelRedefines extends WordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook1 =
    """         01  ENTITY1.
           05  A    PIC X.
    """

  private val copybook2 =
    """         01  ENTITY2.
           05  B    PIC X.
    """

  private val copybookMerged =
    """      01 ROOT.
                05  ENTITY1.
                  10  A    PIC X.
                05  ENTITY2 REDEFINES ENTITY1.
                  10  B    PIC X.
    """

  private val copybook2Roots =
    """         05  ENTITY1.
                  10  A    PIC X.
                05  ENTITY2 REDEFINES ENTITY1.
                  10  B    PIC X.
    """

  val textFileContents: String = "1\n2\n3\n4"

  "Test ASCII CRLF text file " should {
    "with merged copybooks" in {
      withTempDirectory("merged_copybook") { tempDir =>
        val copybook1Path = Paths.get(tempDir, "copybook1.cpy")
        val copybook2Path = Paths.get(tempDir, "copybook2.cpy")

        Files.write(copybook1Path, copybook1.getBytes)
        Files.write(copybook2Path, copybook2.getBytes)


        withTempTextFile("merged_crlf", ".dat", StandardCharsets.UTF_8, textFileContents) { tmpFileName =>
          val df = spark
            .read
            .format("cobol")
            .option("copybooks", s"$copybook1Path,$copybook2Path")
            .option("pedantic", "true")
            .option("is_text", "true")
            .option("encoding", "ascii")
            .option("schema_retention_policy", "collapse_root")
            .load(tmpFileName)

          // This is an error. Should be:
          val expected = """[{"A":"1","B":"1"},{"A":"2","B":"2"},{"A":"3","B":"3"},{"A":"4","B":"4"}]"""

          val count = df.count()
          val actual = df.toJSON.collect().mkString("[", ",", "]")

          assert(count == 4)
          assertEqualsMultiline(actual, expected)
        }
      }
    }

    "with a single copybook" in {
      val expectedLayout =
      """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
        |
        |5 ENTITY1                              r              1      1      1      1
        |  10 A                                                2      1      1      1
        |5 ENTITY2                              R              3      1      1      1
        |  10 B                                                4      1      1      1
        |""".stripMargin
      withTempTextFile("merged_crlf", ".dat", StandardCharsets.UTF_8, textFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybookMerged)
          .option("pedantic", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .option("schema_retention_policy", "collapse_root")
          .load(tmpFileName)

        val expected = """[{"ENTITY1":{"A":"1"},"ENTITY2":{"B":"1"}},{"ENTITY1":{"A":"2"},"ENTITY2":{"B":"2"}},{"ENTITY1":{"A":"3"},"ENTITY2":{"B":"3"}},{"ENTITY1":{"A":"4"},"ENTITY2":{"B":"4"}}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")
        val actualLayout = CopybookParser.parseTree(copybook2Roots).generateRecordLayoutPositions()

        assert(count == 4)
        assertEqualsMultiline(actual, expected)
        assertEqualsMultiline(actualLayout, expectedLayout)
      }
    }

    "with a single multi-root copybook with redefines & collapse root" in {
      val expectedLayout =
        """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
          |
          |5 ENTITY1                              r              1      1      1      1
          |  10 A                                                2      1      1      1
          |5 ENTITY2                              R              3      1      1      1
          |  10 B                                                4      1      1      1
          |""".stripMargin
      withTempTextFile("merged_crlf", ".dat", StandardCharsets.UTF_8, textFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook2Roots)
          .option("pedantic", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .option("schema_retention_policy", "collapse_root")
          .load(tmpFileName)

        val actualLayout = CopybookParser.parseTree(copybook2Roots).generateRecordLayoutPositions()
        // This is an error. Should be:
        val expected = """[{"A":"1","B":"1"},{"A":"2","B":"2"},{"A":"3","B":"3"},{"A":"4","B":"4"}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 4)
        assertEqualsMultiline(actual, expected)
        assertEqualsMultiline(actualLayout, expectedLayout)
      }
    }

    "with a single multi-root copybook with redefines & keep_original" in {
      val expectedLayout =
        """-------- FIELD LEVEL/NAME --------- --ATTRIBS--    FLD  START     END  LENGTH
          |
          |5 ENTITY1                              r              1      1      1      1
          |  10 A                                                2      1      1      1
          |5 ENTITY2                              R              3      1      1      1
          |  10 B                                                4      1      1      1
          |""".stripMargin
      withTempTextFile("merged_crlf", ".dat", StandardCharsets.UTF_8, textFileContents) { tmpFileName =>
        val df = spark
          .read
          .format("cobol")
          .option("copybook_contents", copybook2Roots)
          .option("pedantic", "true")
          .option("is_text", "true")
          .option("encoding", "ascii")
          .option("schema_retention_policy", "keep_original")
          .load(tmpFileName)

        val actualLayout = CopybookParser.parseTree(copybook2Roots).generateRecordLayoutPositions()

        val expected = """[{"ENTITY1":{"A":"1"},"ENTITY2":{"B":"1"}},{"ENTITY1":{"A":"2"},"ENTITY2":{"B":"2"}},{"ENTITY1":{"A":"3"},"ENTITY2":{"B":"3"}},{"ENTITY1":{"A":"4"},"ENTITY2":{"B":"4"}}]"""

        val count = df.count()
        val actual = df.toJSON.collect().mkString("[", ",", "]")

        assert(count == 4)
        assertEqualsMultiline(actual, expected)
        assertEqualsMultiline(actualLayout, expectedLayout)
      }
    }

  }
}
