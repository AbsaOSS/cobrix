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

import java.io.{DataOutputStream, FileOutputStream}

import org.apache.commons.io.{FileUtils => CommonsFileUtils}
import org.apache.spark.sql.functions._
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.TempDir

class Test03IbmFloats extends FunSuite with BeforeAndAfterAll with SparkTestBase {

  private val baseTestDir = TempDir.getNew

  private val copybook =
    """       01  R.
                03 F       COMP-1.
                03 D       COMP-2.
    """

  private val ibmBigEndianPath = baseTestDir + "/ibm_fp_big_endian.dat"
  private val ibmLittleEndianPath = baseTestDir + "/ibm_fp_little_endian.dat"

  private val ieee754BigEndianPath = baseTestDir + "/ieee754_fp_big_endian.dat"
  private val ieee754LittleEndianPath = baseTestDir + "/ieee754_little_endian.dat"

  def assertFloatEqual(a: Float, b: Float): Unit = {
    assert(Math.abs(a - b) < 0.00001, s"($a != $b)")
  }

  def assertDoubleEqual(a: Double, b: Double): Unit = {
    assert(Math.abs(a - b) < 0.0000000001, s"($a != $b)")
  }

  override def beforeAll(): Unit = {
    if (!baseTestDir.exists() || !baseTestDir.isDirectory) {
      throw new IllegalArgumentException(s"Could not create test dir at ${baseTestDir.getAbsolutePath}")
    }

    val dataIbmBe = new DataOutputStream(new FileOutputStream(ibmBigEndianPath))
    (1 to 10).foreach { idx =>
      dataIbmBe.write(Array[Byte](0x00, 0x00, 0x0C.toByte, 0x00,
        0x43.toByte, 0x14.toByte, 0x2E.toByte, 0xFC.toByte,
        0x43.toByte, 0x14.toByte, 0x2E.toByte, 0xFC.toByte,
        0xCA.toByte, 0xF7.toByte, 0x09.toByte, 0xB7.toByte
      ))
    }
    dataIbmBe.close()

    val dataIbmLe = new DataOutputStream(new FileOutputStream(ibmLittleEndianPath))
    (1 to 10).foreach { idx =>
      dataIbmLe.write(Array[Byte](0x00, 0x00, 0x0C.toByte, 0x00,
        0xFC.toByte, 0x2E.toByte, 0x14.toByte, 0x43.toByte,
        0xB7.toByte, 0x09.toByte, 0xF7.toByte, 0xCA.toByte,
        0xFC.toByte, 0x2E.toByte, 0x14.toByte, 0x43.toByte
      ))
    }
    dataIbmLe.close()

    val dataIeee754Be = new DataOutputStream(new FileOutputStream(ieee754BigEndianPath))
    (1 to 10).foreach { idx =>
      dataIeee754Be.write(Array[Byte](0x00, 0x00, 0x0C.toByte, 0x00,
        0x40.toByte, 0x49.toByte, 0x0F.toByte, 0xDA.toByte,
        0x40.toByte, 0x09.toByte, 0x21.toByte, 0xFB.toByte,
        0x54.toByte, 0x44.toByte, 0x2E.toByte, 0xEA.toByte
      ))
    }
    dataIeee754Be.close()

    val dataIeee754Le = new DataOutputStream(new FileOutputStream(ieee754LittleEndianPath))
    (1 to 10).foreach { idx =>
      dataIeee754Le.write(Array[Byte](0x00, 0x00, 0x0C.toByte, 0x00,
        0xDA.toByte, 0x0F.toByte, 0x49.toByte, 0x40.toByte,
        0xEA.toByte, 0x2E.toByte, 0x44.toByte, 0x54.toByte,
        0xFB.toByte, 0x21.toByte, 0x09.toByte, 0x40.toByte
      ))
    }
    dataIeee754Le.close()
  }

  override def afterAll() {
    CommonsFileUtils.deleteDirectory(baseTestDir)
  }

  test("Test data with IBM big endian floating point numbers is parsed correctly") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("is_xcom", true)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IBM")
      .load(ibmBigEndianPath)

    val f = df.select(col("F")).take(1)(0)(0).asInstanceOf[Float]
    val d = df.select(col("D")).take(1)(0)(0).asInstanceOf[Double]

    assertFloatEqual(f, 5.045883f)
    assertDoubleEqual(d, 322.936717)
  }

  test("Test data with IBM little endian floating point numbers is parsed correctly") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("is_xcom", true)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IBM_little_endian")
      .load(ibmLittleEndianPath)

    val f = df.select(col("F")).take(1)(0)(0).asInstanceOf[Float]
    val d = df.select(col("D")).take(1)(0)(0).asInstanceOf[Double]

    assertFloatEqual(f, 5.045883f)
    assertDoubleEqual(d, 322.936717)
  }

  test("Test data with IEEE754 big endian floating point numbers is parsed correctly") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("is_xcom", true)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IEEE754")
      .load(ieee754BigEndianPath)

    val f = df.select(col("F")).take(1)(0)(0).asInstanceOf[Float]
    val d = df.select(col("D")).take(1)(0)(0).asInstanceOf[Double]

    assertFloatEqual(f, 3.1415925f)
    assertDoubleEqual(d, 3.14159265359)
  }

  test("Test data with IEEE754 little endian floating point numbers is parsed correctly") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("is_xcom", true)
      .option("schema_retention_policy", "collapse_root")
      .option("floating_point_format", "IEEE754_little_endian")
      .load(ieee754LittleEndianPath)

    val f = df.select(col("F")).take(1)(0)(0).asInstanceOf[Float]
    val d = df.select(col("D")).take(1)(0)(0).asInstanceOf[Double]

    assertFloatEqual(f, 3.1415925f)
    assertDoubleEqual(d, 3.14159265359)
  }

}
