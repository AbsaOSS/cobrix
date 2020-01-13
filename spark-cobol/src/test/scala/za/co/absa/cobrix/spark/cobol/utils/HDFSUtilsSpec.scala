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

package za.co.absa.cobrix.spark.cobol.utils

import java.io.File
import java.nio.charset.Charset
import java.util.UUID

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}
import org.spark_project.guava.io.Files
import org.apache.commons.io.{FileUtils => CommonsFileUtils}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

class HDFSUtilsSpec extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  private val baseTestDir = TempDir.getNew
  private val validFile = new File(baseTestDir, "a_valid_file")
  private val invalidFile = new File(baseTestDir, "invalid_file")
  private val fileSystem = FileSystem.get(new Configuration())

  override def beforeAll(): Unit = {
    Files.write(UUID.randomUUID().toString, validFile, Charset.defaultCharset())
    invalidFile.mkdir()
  }

  override def afterAll(): Unit = {
    CommonsFileUtils.deleteDirectory(baseTestDir)
  }

  behavior of HDFSUtils.getClass.getName

  it should "throw if offsets are invalid" in {

    val errorLength = intercept[IllegalArgumentException] {
     HDFSUtils.getBlocksLocations(toHDFSPath(validFile), 0, 0, fileSystem)
    }
    assert(errorLength.getMessage.contains("Invalid"))

    val errorOffset = intercept[IllegalArgumentException] {
      HDFSUtils.getBlocksLocations(toHDFSPath(validFile), -1, 10, fileSystem)
    }
    assert(errorOffset.getMessage.contains("Invalid"))
  }

  it should "throw if file is actually a directory" in {

    val error = intercept[IllegalArgumentException] {
      HDFSUtils.getBlocksLocations(toHDFSPath(invalidFile), 0, 10, fileSystem)
    }

    assert(!error.getMessage.isEmpty)
  }

  it should "return the correct locations for the files blocks" in {
    val locations = HDFSUtils.getBlocksLocations(toHDFSPath(validFile), 0, 10, fileSystem)

    assert(locations.size == 1)
    assert(locations.head == "localhost")
  }

  private def toHDFSPath(file: File) = new Path(file.getAbsolutePath)
}