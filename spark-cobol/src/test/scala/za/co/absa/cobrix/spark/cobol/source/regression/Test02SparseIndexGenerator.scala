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
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.TempDir

// These examples are provided by Peter Moon

class Test02SparseIndexGenerator extends FunSuite with BeforeAndAfterAll with SparkTestBase {

  private val baseTestDir = TempDir.getNew

  private val copybook =
    """       01  R.
           03 I       PIC 9(1).
           03 D       PIC 9(1).
    """

  private val dataWithHeaderPath = baseTestDir + "/data_with_header.dat"
  private val dataWithoutHeaderPath = baseTestDir + "/data_without_header.dat"
  private val dataWithHeaderOnly = baseTestDir + "/header_only.dat"

  override def beforeAll(): Unit = {
    if (!baseTestDir.exists() || !baseTestDir.isDirectory) {
      throw new IllegalArgumentException(s"Could not create test dir at ${baseTestDir.getAbsolutePath}")
    }

    val data = new DataOutputStream(new FileOutputStream(dataWithHeaderPath))
    data.write(Array[Byte](0x00, 0x00, 0x01, 0x00, 0xF0.toByte))
    (1 to 9).foreach { idx =>
      data.write(Array[Byte](0x00, 0x00, 0x02, 0x00, 0xF1.toByte, (0xF0 + idx).toByte))
    }
    data.close()

    val data2 = new DataOutputStream(new FileOutputStream(dataWithoutHeaderPath))
    (1 to 9).foreach { idx =>
      data2.write(Array[Byte](0x00, 0x00, 0x02, 0x00, 0xF1.toByte, (0xF0 + idx).toByte))
    }
    data2.close()

    val data3 = new DataOutputStream(new FileOutputStream(dataWithHeaderOnly))
    data3.write(Array[Byte](0x00, 0x00, 0x01, 0x00, 0xF0.toByte))
    data3.close()
  }

  override def afterAll() {
    CommonsFileUtils.deleteDirectory(baseTestDir)
  }

  test("Test data with header, no segment filter, no segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .load(dataWithHeaderPath)
    println("Data With Header, No segment options # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 2)
    assert(df.count() == 10)
  }

  test("Test data with header, segment filter, no segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1")
      .load(dataWithHeaderPath)
    println("Data With Header, segment field and filter specified # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 2)
    assert(df.count() == 9)
  }

  test("Test data with header, segment filter, segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1,0")
      .option("segment_id_root", "1")
      .load(dataWithHeaderPath)
    println("Data With Header, segment field, filter and id root specified # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 2)
    assert(df.count() == 9)
  }

  test("Test data without header, no segment filter, no segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .load(dataWithoutHeaderPath)
    println("Data Without Header, No segment options # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 2)
    assert(df.count() == 9)
  }

  test("Test data without header, segment filter, no segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1")
      .load(dataWithoutHeaderPath)
    println("Data Without Header, segment field and filter specified # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 2)
    assert(df.count() == 9)
  }

  test("Test data without header, segment filter, segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1,0")
      .option("segment_id_root", "1")
      .load(dataWithoutHeaderPath)
    println("Data Without Header, segment field, filter and id root specified # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 2)
    assert(df.count() == 9)
  }

  test("Test header only, no segment filter, no segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .load(dataWithHeaderOnly)
    println("Data With Header only, No segment options # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 1)
    assert(df.count() == 1)
  }

  test("Test header only, segment filter, no segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1")
      .load(dataWithHeaderOnly)
    println("Data Header Only, segment field and filter specified # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 1)
    assert(df.count() == 0)
  }

  test("Test header only, segment filter, segment root") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1,0")
      .option("segment_id_root", "1")
      .load(dataWithHeaderOnly)
    println("Data Header Only, segment field, filter and id root specified # partitions: " + df.rdd.partitions.size)
    assert(df.rdd.partitions.size == 1)
    assert(df.count() == 0)
  }

}
