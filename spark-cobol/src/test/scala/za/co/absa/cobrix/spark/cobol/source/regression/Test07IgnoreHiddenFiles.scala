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
import java.nio.file.{FileSystem, Files, Path, Paths}

import org.apache.hadoop.fs.{FileSystem => HadoopFs}
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase
import za.co.absa.cobrix.spark.cobol.source.fixtures.BinaryFileFixture
import za.co.absa.cobrix.spark.cobol.utils.FileUtils

class Test07IgnoreHiddenFiles extends AnyFunSuite with BinaryFileFixture with SparkTestBase {
  private val fileSystem = HadoopFs.get(spark.sparkContext.hadoopConfiguration)

  test("Test findAndLogFirstNonDivisibleFile() finds a file") {
    withTempDirectory("testHidden1") { tmpDir =>
      createFileSize1(Files.createFile(Paths.get(tmpDir, "a-file.dat")))

      val nonDivisibleFileOpt = FileUtils.findAndLogFirstNonDivisibleFile(tmpDir, 2, fileSystem)
      val nonDivisibleFiles = FileUtils.findAndLogAllNonDivisibleFiles(tmpDir, 2, fileSystem)

      assert(nonDivisibleFileOpt.isDefined)
      assert(nonDivisibleFileOpt.get._1.endsWith("a-file.dat"))
      assert(nonDivisibleFileOpt.get._2 == 1)

      assert(nonDivisibleFiles.length == 1)
      assert(nonDivisibleFiles.head._2 == 1)
      assert(nonDivisibleFiles.head._1.endsWith("a-file.dat"))
    }
  }

  test("Test findAndLogFirstNonDivisibleFile() ignores a hidden file") {
    withTempDirectory("testHidden1") { tmpDir =>
      createFileSize1(Files.createFile(Paths.get(tmpDir, ".a")))
      val nonDivisibleFileOpt = FileUtils.findAndLogFirstNonDivisibleFile(tmpDir, 2, fileSystem)
      val nonDivisibleFiles = FileUtils.findAndLogAllNonDivisibleFiles(tmpDir, 2, fileSystem)

      assert(nonDivisibleFileOpt.isEmpty)
      assert(nonDivisibleFiles.isEmpty)
    }
  }

  test("Test findAndLogFirstNonDivisibleFile() ignores a  hidden file in a nested dir") {
    withTempDirectory("testHidden3") { tmpDir =>
      Files.createDirectory(Paths.get(tmpDir, "dir1"))
      createFileSize1(Files.createFile(Paths.get(tmpDir, "dir1", ".b2")))
      val nonDivisibleFileOpt = FileUtils.findAndLogFirstNonDivisibleFile(tmpDir, 2, fileSystem)
      val nonDivisibleFiles = FileUtils.findAndLogAllNonDivisibleFiles(tmpDir, 2, fileSystem)

      assert(nonDivisibleFileOpt.isEmpty)
      assert(nonDivisibleFiles.isEmpty)
    }
  }

  test("Test findAndLogFirstNonDivisibleFile() ignores a hidden dir") {
    withTempDirectory("testHidden4") { tmpDir =>
      Files.createDirectory(Paths.get(tmpDir, ".dir2"))
      createFileSize1(Files.createFile(Paths.get(tmpDir, ".dir2", "c1")))
      val nonDivisibleFileOpt = FileUtils.findAndLogFirstNonDivisibleFile(tmpDir, 2, fileSystem)
      val nonDivisibleFiles = FileUtils.findAndLogAllNonDivisibleFiles(tmpDir, 2, fileSystem)

      assert(nonDivisibleFileOpt.isEmpty)
      assert(nonDivisibleFiles.isEmpty)
    }
  }

  test("Test findAndLogFirstNonDivisibleFile() works with globbing") {
    withTempDirectory("testHidden1") { tmpDir =>
      createFileSize1(Files.createFile(Paths.get(tmpDir, "a.dat")))

      val nonDivisibleFileOpt = FileUtils.findAndLogFirstNonDivisibleFile(tmpDir, 2, fileSystem)
      val nonDivisibleFiles = FileUtils.findAndLogAllNonDivisibleFiles(tmpDir, 2, fileSystem)

      assert(nonDivisibleFileOpt.isDefined)
      assert(nonDivisibleFileOpt.get._1.endsWith("a.dat"))
      assert(nonDivisibleFileOpt.get._2 == 1)

      assert(nonDivisibleFiles.length == 1)
      assert(nonDivisibleFiles.head._1.endsWith("a.dat"))
      assert(nonDivisibleFiles.head._2 == 1)
    }
  }


  private def createFileSize1(path: Path): Unit = {
    val file = new File(path.toAbsolutePath.toString)
    val ostream = new DataOutputStream(new FileOutputStream(file))
    val content = Array[Byte](0.toByte)
    ostream.write(content)
    ostream.close()
  }
}
