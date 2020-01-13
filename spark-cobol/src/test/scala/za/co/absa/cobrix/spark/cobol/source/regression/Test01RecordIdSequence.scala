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

import java.io.{DataOutputStream, File, FileOutputStream}

import org.apache.commons.io.FileUtils
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.functions.col
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.utils.TestUtils._

//noinspection NameBooleanParameters
class Test01RecordIdSequence extends FunSuite with BeforeAndAfter with SparkTestBase {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val tmpPath = "../data/temp_test"

  val copybook =
    """      01  R.
                03 I        PIC 9(1).
                03 D        PIC 9(1).
    """

  private val fileName = new Path(tmpPath, "recorddata.dat").toString

  before {
    FileUtils.forceMkdir(new File(tmpPath))

    val dataStream = new DataOutputStream(new FileOutputStream(fileName))
    dataStream.write(Array[Byte](0x00, 0x00, 0x02, 0x00, 0xF0.toByte, 0xF0.toByte))
    (1 to 9).foreach { idx =>
      dataStream.write(Array[Byte](0x00, 0x00, 0x02, 0x00, 0xF1.toByte, (0xF0 + idx).toByte))
    }
    dataStream.close()
  }

  after {
    FileUtils.deleteDirectory(new File(tmpPath))
  }

  test(s"Regression test on inconsistent generated record IDs") {
    // This example is provided by Peter Moon

    val expected1 =
      """+---------+---+---+
        ||Record_Id|I  |D  |
        |+---------+---+---+
        ||0        |0  |0  |
        ||1        |1  |1  |
        ||2        |1  |2  |
        ||3        |1  |3  |
        ||4        |1  |4  |
        ||5        |1  |5  |
        ||6        |1  |6  |
        ||7        |1  |7  |
        ||8        |1  |8  |
        ||9        |1  |9  |
        |+---------+---+---+
        |
        |""".stripMargin.replace("\r\n", "\n")

    val expected2 =
      """+---------+---+---+
        ||Record_Id|I  |D  |
        |+---------+---+---+
        ||1        |1  |1  |
        ||2        |1  |2  |
        ||3        |1  |3  |
        ||4        |1  |4  |
        ||5        |1  |5  |
        ||6        |1  |6  |
        ||7        |1  |7  |
        ||8        |1  |8  |
        ||9        |1  |9  |
        |+---------+---+---+
        |
        |""".stripMargin.replace("\r\n", "\n")

    val expected3 =
      """+---------+-------+---+---+
        ||Record_Id|Seg_Id0|I  |D  |
        |+---------+-------+---+---+
        ||1        |i_0_1  |1  |1  |
        ||2        |i_0_2  |1  |2  |
        ||3        |i_0_3  |1  |3  |
        ||4        |i_0_4  |1  |4  |
        ||5        |i_0_5  |1  |5  |
        ||6        |i_0_6  |1  |6  |
        ||7        |i_0_7  |1  |7  |
        ||8        |i_0_8  |1  |8  |
        ||9        |i_0_9  |1  |9  |
        |+---------+-------+---+---+
        |
        |""".stripMargin.replace("\r\n", "\n")


    val dfRecordAll = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .load(fileName)
    logger.debug("Rows Count (No Segment Filter): " + dfRecordAll.count())
    val dfRecordFilterAll = dfRecordAll
    logger.debug("Record_Ids after DataFrame filter")
    val actual1 = showString(dfRecordFilterAll.select(col("Record_Id"), col("R.I"), col("R.D")))
    assert(actual1 == expected1)

    val dfRecordSegmentFilter = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1")
      .load(fileName)
    logger.debug("Row Count (Filtered with Segment Filter): " + dfRecordSegmentFilter.count())

    logger.debug("Record_Ids after DataFrame filter")
    val dfRecordFilterSegmentFilter = dfRecordSegmentFilter
    val actual2 = showString(dfRecordFilterSegmentFilter.select(col("Record_Id"), col("R.I"), col("R.D")))
    assert(actual2 == expected2)

    val dfRecordSegmentFilterWithRootSegment = spark
      .read
      .format("cobol")
      .option("copybook_contents", copybook)
      .option("generate_record_id", true)
      .option("input_split_records", 5)
      .option("is_xcom", true)
      .option("segment_field", "I")
      .option("segment_filter", "1")
      .option("segment_id_root", "1")
      .option("segment_id_prefix", "i")
      .load(fileName)
    logger.debug("Row Count (Filtered with Segment Filter and Root Segment): " + dfRecordSegmentFilter.count())

    logger.debug("Record_Ids after DataFrame filter")
    val dfRecordSegmentFilterWithRootSegmentFilter = dfRecordSegmentFilterWithRootSegment
    val actual3 = showString(dfRecordSegmentFilterWithRootSegmentFilter.select(col("Record_Id"), col("Seg_Id0"), col("R.I"), col("R.D")))
    assert(actual3 == expected3)
  }

}
