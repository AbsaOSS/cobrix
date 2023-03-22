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

package za.co.absa.cobrix.spark.cobol.source.integration

import org.apache.spark.sql.types.{ArrayType, StructType}
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.cobrix.spark.cobol.source.base.{SimpleComparisonBase, SparkTestBase}
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture

import java.nio.charset.StandardCharsets

class Test33Metadata extends AnyWordSpec with SparkTestBase with BinaryFileFixture with SimpleComparisonBase {
  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val copybook =
    """         01 RECORD.
        02 COUNT PIC 9(1).
        02 GROUP OCCURS 0 TO 2 TIMES DEPENDING ON COUNT.
           03 INNER-COUNT PIC 9(1).
           03 INNER-GROUP OCCURS 0 TO 3 TIMES
                              DEPENDING ON INNER-COUNT.
              04 FIELD PIC X.
        02 P-GROUP COMP-3.
           03 FLD1 PIC 9(6)V99.
           03 FLD2 PIC S9(5) REDEFINES FLD1.
    """

  private val data = "12345"

  "no metadata" should {
    "don't add metadata to the Spark schema" in {
      withTempTextFile("record_length", ".bin", StandardCharsets.UTF_8, data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("metadata", "false")
          .option("pedantic", "true")
          .load(fileName)

        val schema = df.schema

        val countFld = schema.fields.head
        val groupFld = schema.fields(1)
        val pGroupFld = schema.fields(2)
        val arrayFields = groupFld.dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields
        val innerCountFld = arrayFields.head
        val innerGroupFld = arrayFields(1)
        val innerArrayFields = innerGroupFld.dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields
        val innerInnerField = innerArrayFields.head
        val pGroupField1 = pGroupFld.dataType.asInstanceOf[StructType].fields.head
        val pGroupField2 = pGroupFld.dataType.asInstanceOf[StructType].fields(1)

        assert(countFld.name == "COUNT")
        assert(groupFld.name == "GROUP")
        assert(innerCountFld.name == "INNER_COUNT")
        assert(innerGroupFld.name == "INNER_GROUP")
        assert(innerInnerField.name == "FIELD")
        assert(pGroupFld.name == "P_GROUP")
        assert(pGroupField1.name == "FLD1")
        assert(pGroupField2.name == "FLD2")

        assert(countFld.metadata.toString() == "{}")
        assert(groupFld.metadata.toString() == "{}")
        assert(innerCountFld.metadata.toString() == "{}")
        assert(innerGroupFld.metadata.toString() == "{}")
        assert(innerInnerField.metadata.toString() == "{}")
        assert(countFld.metadata.toString() == "{}")
        assert(pGroupField1.metadata.toString() == "{}")
        assert(pGroupField2.metadata.toString() == "{}")
      }
    }
  }

  "basic metadata" should {
    "add basic metadata to the Spark schema" in {
      withTempTextFile("record_length", ".bin", StandardCharsets.UTF_8, data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("metadata", "basic")
          .option("pedantic", "true")
          .load(fileName)

        val schema = df.schema

        val countFld = schema.fields.head
        val groupFld = schema.fields(1)
        val pGroupFld = schema.fields(2)
        val arrayFields = groupFld.dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields
        val innerCountFld = arrayFields.head
        val innerGroupFld = arrayFields(1)
        val innerArrayFields = innerGroupFld.dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields
        val innerInnerField = innerArrayFields.head
        val pGroupField1 = pGroupFld.dataType.asInstanceOf[StructType].fields.head
        val pGroupField2 = pGroupFld.dataType.asInstanceOf[StructType].fields(1)

        assert(countFld.name == "COUNT")
        assert(groupFld.name == "GROUP")
        assert(innerCountFld.name == "INNER_COUNT")
        assert(innerGroupFld.name == "INNER_GROUP")
        assert(innerInnerField.name == "FIELD")
        assert(pGroupFld.name == "P_GROUP")
        assert(pGroupField1.name == "FLD1")
        assert(pGroupField2.name == "FLD2")

        assert(countFld.metadata.toString() == "{}")
        assert(innerCountFld.metadata.toString() == "{}")
        assert(innerInnerField.metadata.toString() == "{\"maxLength\":1}")
        assert(countFld.metadata.toString() == "{}")
        assert(pGroupField1.metadata.toString() == "{}")
        assert(pGroupField2.metadata.toString() == "{}")

        assert(groupFld.metadata.getLong("minElements") == 0)
        assert(groupFld.metadata.getLong("maxElements") == 2)

        assert(innerGroupFld.metadata.getLong("minElements") == 0)
        assert(innerGroupFld.metadata.getLong("maxElements") == 3)
      }
    }
  }

  "extended_metadata" should {
    "add more metadata to the Spark schema" in {
      withTempTextFile("record_length", ".bin", StandardCharsets.UTF_8, data) { fileName =>
        val df = spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("metadata", "extended")
          .option("pedantic", "true")
          .load(fileName)

        val schema = df.schema

        val countFld = schema.fields.head
        val groupFld = schema.fields(1)
        val pGroupFld = schema.fields(2)
        val arrayFields = groupFld.dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields
        val innerCountFld = arrayFields.head
        val innerGroupFld = arrayFields(1)
        val innerArrayFields = innerGroupFld.dataType.asInstanceOf[ArrayType].elementType.asInstanceOf[StructType].fields
        val innerInnerField = innerArrayFields.head
        val pGroupField1 = pGroupFld.dataType.asInstanceOf[StructType].fields.head
        val pGroupField2 = pGroupFld.dataType.asInstanceOf[StructType].fields(1)

        assert(countFld.name == "COUNT")
        assert(groupFld.name == "GROUP")
        assert(innerCountFld.name == "INNER_COUNT")
        assert(innerGroupFld.name == "INNER_GROUP")
        assert(innerInnerField.name == "FIELD")
        assert(pGroupFld.name == "P_GROUP")
        assert(pGroupField1.name == "FLD1")
        assert(pGroupField2.name == "FLD2")

        assert(countFld.metadata.getLong("level") == 2)
        assert(groupFld.metadata.getLong("level") == 2)
        assert(innerCountFld.metadata.getLong("level") == 3)
        assert(innerGroupFld.metadata.getLong("level") == 3)
        assert(innerInnerField.metadata.getLong("level") == 4)
        assert(pGroupFld.metadata.getLong("level") == 2)
        assert(pGroupField1.metadata.getLong("level") == 3)

        assert(countFld.metadata.getString("pic") == "9(1)")
        assert(innerCountFld.metadata.getString("pic") == "9(1)")
        assert(innerInnerField.metadata.getString("pic") == "X")
        assert(pGroupField1.metadata.getString("pic") == "9(6)V99")

        assert(countFld.metadata.getLong("precision") == 1)
        assert(innerCountFld.metadata.getLong("precision") == 1)
        assert(pGroupField1.metadata.getLong("precision") == 8)
        assert(pGroupField2.metadata.getLong("precision") == 5)

        assert(!countFld.metadata.getBoolean("signed"))
        assert(!innerCountFld.metadata.getBoolean("signed"))
        assert(!pGroupField1.metadata.getBoolean("signed"))
        assert(pGroupField2.metadata.getBoolean("signed"))

        assert(pGroupField1.metadata.getLong("scale") == 2)

        assert(countFld.metadata.getLong("offset") == 0)
        assert(groupFld.metadata.getLong("offset") == 1)
        assert(innerCountFld.metadata.getLong("offset") == 1)
        assert(innerGroupFld.metadata.getLong("offset") == 2)
        assert(innerInnerField.metadata.getLong("offset") == 2)
        assert(pGroupFld.metadata.getLong("offset") == 9)
        assert(pGroupField1.metadata.getLong("offset") == 9)
        assert(pGroupField2.metadata.getLong("offset") == 9)

        assert(countFld.metadata.getLong("byte_size") == 1)
        assert(groupFld.metadata.getLong("byte_size") == 4)
        assert(innerCountFld.metadata.getLong("byte_size") == 1)
        assert(innerGroupFld.metadata.getLong("byte_size") == 1)
        assert(innerInnerField.metadata.getLong("byte_size") == 1)
        assert(pGroupFld.metadata.getLong("byte_size") == 5)
        assert(pGroupField1.metadata.getLong("byte_size") == 5)
        assert(pGroupField2.metadata.getLong("byte_size") == 3)

        assert(pGroupField1.metadata.getBoolean("implied_decimal"))
        assert(!pGroupField1.metadata.getBoolean("sign_separate"))

        assert(groupFld.metadata.getLong("minElements") == 0)
        assert(groupFld.metadata.getLong("maxElements") == 2)
        assert(groupFld.metadata.getString("depending_on") == "COUNT")

        assert(pGroupFld.metadata.getString("usage") == "COMP-3")
        assert(pGroupField1.metadata.getString("usage") == "COMP-3")
        assert(pGroupField2.metadata.getString("usage") == "COMP-3")
        assert(pGroupField2.metadata.getString("redefines") == "FLD1")

        assert(pGroupFld.metadata.getString("originalName") == "P-GROUP")
        assert(innerGroupFld.metadata.getString("originalName") == "INNER-GROUP")
        assert(innerCountFld.metadata.getString("originalName") == "INNER-COUNT")
      }
    }
  }

  "wrong metadata polity" in {
    withTempTextFile("record_length", ".bin", StandardCharsets.UTF_8, data) { fileName =>
      assertThrows[IllegalArgumentException] {
        spark.read
          .format("cobol")
          .option("copybook_contents", copybook)
          .option("record_format", "D")
          .option("metadata", "something")
          .option("pedantic", "true")
          .load(fileName)
      }
    }
  }
}
