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

package za.co.absa.cobrix.spark.cobol.source.index

import org.apache.hadoop.fs._
import org.apache.hadoop.hdfs.DistributedFileSystem
import org.mockito.Mockito.{mock, when => whenMock}
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.reader.common.Constants
import za.co.absa.cobrix.cobol.reader.index.entry.SparseIndexEntry
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.spark.cobol.reader.{Reader, VarLenNestedReader}
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.source.parameters.LocalityParameters
import za.co.absa.cobrix.spark.cobol.source.types.FileWithOrder
import za.co.absa.cobrix.spark.cobol.source.utils.SourceTestUtils.createFile

import java.io.File
import java.nio.file.{Files, Paths}

class IndexBuilderSpec extends AnyWordSpec with BinaryFileFixture with SparkTestBase {
  private val copybook =
    """       01  RECORD.
             05  A       PIC X(5).
      """

  private val records = Array(
    0x00, 0x04, 0x00, 0x00, 0xF0,
    0xF1, 0xF2, 0xF3, 0x00, 0x05,
    0x00, 0x00, 0xF4, 0xF5, 0xF6,
    0xF7, 0xF8
  ).map(_.toByte)


  "buildIndex()" should {
    "generate index with data locality" in {
      withTempDirectory("index_builder") { tempDir =>
        val file = createFile(new File(tempDir), "test_file", records)

        val filesWithOrder = Array(
          FileWithOrder(file.getAbsolutePath, 0)
        )

        val readerParameters = ReaderParameters(isIndexGenerationNeeded = true,
          inputSplitRecords = Some(1)
        )

        val reader = new VarLenNestedReader(Seq(copybook), readerParameters)

        val localityParameters = LocalityParameters(improveLocality = true, optimizeAllocation = true)

        val index = IndexBuilder.buildIndex(filesWithOrder, reader, spark.sqlContext)(localityParameters).collect()

        assert(index.length == 3)
      }
    }

    "generate index without data locality" in {
      withTempDirectory("index_builder") { tempDir =>
        val file = createFile(new File(tempDir), "test_file", records)

        val filesWithOrder = Array(
          FileWithOrder(file.getAbsolutePath, 0)
        )

        val readerParameters = ReaderParameters(isIndexGenerationNeeded = true,
          inputSplitRecords = Some(1)
        )

        val reader = new VarLenNestedReader(Seq(copybook), readerParameters)

        val localityParameters = LocalityParameters(improveLocality = false, optimizeAllocation = false)

        val index = IndexBuilder.buildIndex(filesWithOrder, reader, spark.sqlContext)(localityParameters).collect()

        assert(index.length == 3)
      }
    }

    "build indexes for files for which indexing is not supported" in {
      withTempDirectory("index_builder") { tempDir =>
        val file = createFile(new File(tempDir), "test_file", records)

        val filesWithOrder = Array(
          FileWithOrder(file.getAbsolutePath, 0)
        )

        val reader = mock(classOf[Reader])

        val localityParameters = LocalityParameters(improveLocality = false, optimizeAllocation = false)

        val index = IndexBuilder.buildIndex(filesWithOrder, reader, spark.sqlContext)(localityParameters).collect()

        assert(index.length == 1)
      }
    }
  }

  "buildIndexForVarLenReaderWithFullLocality()" should {
    "build indexes for files taking into account data locality" in {
      withTempDirectory("index_builder") { tempDir =>
        val file = createFile(new File(tempDir), "test_file", records)

        val filesWithOrder = Array(
          FileWithOrder(file.getAbsolutePath, 0)
        )

        val readerParameters = ReaderParameters(isIndexGenerationNeeded = true,
          inputSplitRecords = Some(1)
        )

        val reader = new VarLenNestedReader(Seq(copybook), readerParameters)

        val index = IndexBuilder.buildIndexForVarLenReaderWithFullLocality(filesWithOrder, reader, spark.sqlContext, optimizeAllocation = false).collect()

        assert(index.length == 3)
      }
    }

    "build indexes for files taking into account data locality with optimized allocation" in {
      withTempDirectory("index_builder") { tempDir =>
        val file = createFile(new File(tempDir), "test_file", records)

        val filesWithOrder = Array(
          FileWithOrder(file.getAbsolutePath, 0)
        )

        val readerParameters = ReaderParameters(isIndexGenerationNeeded = true,
          inputSplitRecords = Some(1)
        )

        val reader = new VarLenNestedReader(Seq(copybook), readerParameters)

        val index = IndexBuilder.buildIndexForVarLenReaderWithFullLocality(filesWithOrder, reader, spark.sqlContext, optimizeAllocation = true).collect()

        assert(index.length == 3)
      }
    }
  }

  "buildIndexForVarLenReader()" should {
    "build indexes for variable record length files" in {
      withTempDirectory("index_builder") { tempDir =>
        val file = createFile(new File(tempDir), "test_file", records)

        val filesWithOrder = Array(
          FileWithOrder(file.getAbsolutePath, 0)
        )

        val readerParameters = ReaderParameters(isIndexGenerationNeeded = true,
          inputSplitRecords = Some(1)
        )

        val reader = new VarLenNestedReader(Seq(copybook), readerParameters)

        val index = IndexBuilder.buildIndexForVarLenReader(filesWithOrder, reader, spark.sqlContext).collect()

        assert(index.length == 3)
      }
    }
    "build indexes for variable record length files with fileEndOffset" in {
      withTempDirectory("index_builder") { tempDir =>
        val file = createFile(new File(tempDir), "test_file", records)

        val filesWithOrder = Array(
          FileWithOrder(file.getAbsolutePath, 0)
        )

        val readerParameters = ReaderParameters(isIndexGenerationNeeded = true,
          inputSplitRecords = Some(1),
          fileEndOffset = 3
        )

        val reader = new VarLenNestedReader(Seq(copybook), readerParameters)

        val index = IndexBuilder.buildIndexForVarLenReader(filesWithOrder, reader, spark.sqlContext).collect()

        assert(index.length == 2)
      }
    }
  }

  "buildIndexForFullFiles()" should {
    "generate a spare index placeholder for each file" in {
      val files = Array(
        FileWithOrder("dummy_file1", 0),
        FileWithOrder("dummy_file2", 1),
        FileWithOrder("dummy_file3", 2),
        FileWithOrder("dummy_file4", 3)
      )

      val sparseIndexRDD = IndexBuilder.buildIndexForFullFiles(files, spark.sqlContext)

      val index = sparseIndexRDD.collect()

      assert(sparseIndexRDD.count() == 4)
      assert(index.forall(_.offsetFrom == 0))
      assert(index.forall(_.offsetTo == -1))
      assert(index.forall(_.recordIndex == 0))
      assert(index.exists(_.fileId == 0))
      assert(index.exists(_.fileId == 1))
      assert(index.exists(_.fileId == 2))
      assert(index.exists(_.fileId == 3))
    }
  }

  "getBlockLengthByIndexEntry()" should {
    "work for small records" in {
      val entry = SparseIndexEntry(0, 1024, 0, 0)

      assert(IndexBuilder.getBlockLengthByIndexEntry(entry) == 1024)
    }

    "work for big records" in {
      val entry = SparseIndexEntry(0, 20*Constants.megabyte, 0, 0)

      assert(IndexBuilder.getBlockLengthByIndexEntry(entry) == 19*Constants.megabyte)
    }
  }

  "toRDDWithLocality()" should {
    "convert the list of files to RDD" in {
      withTempDirectory("indxe_builder") { tempDir =>
        Files.createFile(Paths.get(tempDir, "text1.txt"))
        Files.createFile(Paths.get(tempDir, "text2.txt"))

        val files = Array(FileWithOrder(Paths.get(tempDir, "text1.txt").toString, 0),
          FileWithOrder(Paths.get(tempDir, "text2.txt").toString, 1))

        val filesWIthPreferredLocations = IndexBuilder.toRDDWithLocality(files, spark.sparkContext.hadoopConfiguration, spark.sqlContext)

        assert(filesWIthPreferredLocations.count() == 2)
      }
    }
  }

  "isDataLocalitySupported()" should {
    "return true for supported filesystems" in {
      assert(IndexBuilder.isDataLocalitySupported(mock(classOf[DistributedFileSystem])))
    }

    "return false for unsupported filesystems" in {
      val ftpFileSystem = mock(classOf[LocalFileSystem])

      whenMock(ftpFileSystem.getScheme).thenReturn("dummy")
      assert(!IndexBuilder.isDataLocalitySupported(ftpFileSystem))
    }
  }
}
