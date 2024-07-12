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

package za.co.absa.cobrix.spark.cobol.source.base

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.reader.policies.SchemaRetentionPolicy
import za.co.absa.cobrix.spark.cobol.schema.CobolSchema

class SparkSchemaSpec extends AnyFunSuite {

  test("Test Spark schema produced from a copybook with unbreakable spaces (0xA0) and tabs") {
    val c = '\u00a0'
    val t = 0x09.toByte.toChar
    val copyBookContents: String =
      s"""        01  RECORD.
         |            05  F1$c$c$c$c${c}PIC X(10).
         |            05  F2$c$c$c  PIC 9(10).
         |            05 ${c}F3$c$c$c  PIC 9(10).
         |           ${c}05$c${c}F4$c  ${c}PIC 9(10).
         |           ${t}05$t${t}F5$t  ${t}PIC 9(10).
         |""".stripMargin

    val parsedSchema = CopybookParser.parseTree(copyBookContents)

    val cobolSchema = new CobolSchema(parsedSchema, SchemaRetentionPolicy.CollapseRoot, false, "",false, false)

    val sparkSchema = cobolSchema.getSparkSchema

    val field1 = sparkSchema.fields.head.name
    val field2 = sparkSchema.fields(1).name
    val field3 = sparkSchema.fields(2).name
    val field4 = sparkSchema.fields(3).name
    val field5 = sparkSchema.fields(4).name

    assert(field1 == "F1")
    assert(field2 == "F2")
    assert(field3 == "F3")
    assert(field4 == "F4")
    assert(field5 == "F5")
  }

}
