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

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.apache.commons.io.{FileUtils => CommonsFileUtils}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem

class FileUtilsSpec extends FlatSpec with BeforeAndAfterAll {

  private val tmpDir = if (System.getProperty("java.io.tmpdir") != null) System.getProperty("java.io.tmpdir") else ""

  private val baseTestDir = new File(s"$tmpDir/${System.currentTimeMillis()}-test-dir/") // tmpDir/8377740/
  private val innerTestDir1 = new File(s"$baseTestDir/inner1") // tmpDir/8377740/inner1
  private val innerTestDir2 = new File(s"$baseTestDir/inner2") // tmpDir/8377740/inner2
  private val innerTestDir3 = new File(s"$innerTestDir2/inner3") // tmpDir/8377740/inner2/inner3
  private val innerTestDir4 = new File(s"$innerTestDir3/inner4") // tmpDir/8377740/inner2/inner3/inner4

  private val testFiles = Map(
    baseTestDir.getName -> Array("a", ".a", "_a"),
    innerTestDir1.getName -> Array("b", ".b", "_b"),
    innerTestDir2.getName -> Array("c", ".c", "_c"),
    innerTestDir3.getName -> Array("d", ".d", "_d"),
    innerTestDir4.getName -> Array("e", ".e", "_e")
  )

  private val fileSystem = FileSystem.get(new Configuration())

  override def beforeAll(): Unit = {

    baseTestDir.mkdirs()

    if (baseTestDir.exists() && baseTestDir.isDirectory) {
      println(s"Created base test dir at ${baseTestDir.getAbsolutePath}")
    }
    else {
      throw new IllegalArgumentException(s"Could not create test dir at ${baseTestDir.getAbsolutePath}")
    }

    innerTestDir4.mkdirs()
    innerTestDir1.mkdirs()

    Array(baseTestDir, innerTestDir1, innerTestDir2, innerTestDir3, innerTestDir4)
      .foreach(dir => {
        testFiles(dir.getName)
          .foreach(fileName => new File(dir, fileName).createNewFile())
      })
  }

  override def afterAll() {
    println(s"Deleting test directory at ${baseTestDir.getAbsolutePath}")
    CommonsFileUtils.deleteDirectory(baseTestDir)
  }

  behavior of FileUtils.getClass.getName

  it should "retrieve specific file" in {
    var file = new File(innerTestDir3, testFiles(innerTestDir3.getName)(0)).getAbsolutePath
      .replaceAll("\\\\", "/") // Workaround for Windows '\' path separator
    // Workaround for Windows absolute paths that ctart with C:\...
    if (!file.startsWith("/"))
      file = "/" + file
    val paths = FileUtils.getFiles(file, fileSystem, recursive = false)
    assert(paths.size == 1)
    assert(paths.head == file)
  }

  it should "not retrieve files starting with ." in {
    val file = new File(innerTestDir3, testFiles(innerTestDir3.getName)(1)).getAbsolutePath
    val exception = intercept[IllegalArgumentException] {
      FileUtils.getFiles(file, fileSystem, recursive = false)
    }
    assert(exception.getMessage.contains("Input path does not exist"))
  }

  it should "retrieve all files in given directory" in {
    val paths = FileUtils.getFiles(innerTestDir3.getCanonicalPath, fileSystem, recursive = false)
    assert(paths.size == 1, "should have ignored files starting with . or _")
  }

  it should "retrieve only files from directory" in {
    val paths = FileUtils.getFiles(baseTestDir.getCanonicalPath, fileSystem, recursive = false)
    assert(paths.size == 1, "should have ignored files starting with . or _ and directories")
  }

  it should "retrieve files from glob patterns" in {
    val paths = FileUtils.getFiles(baseTestDir.getCanonicalPath + "/*", fileSystem, recursive = false)
    assert(paths.size == 3, "should have ignored files starting with . or _")
  }

  it should "retrieve all files recursively without glob pattern" in {
    val paths = FileUtils.getFiles(baseTestDir.getCanonicalPath, fileSystem, recursive = true)
    assert(paths.size == 5, "should have ignored files starting with . or _")
  }

  it should "retrieve all files recursively with glob pattern" in {
    val paths = FileUtils.getFiles(baseTestDir.getCanonicalPath + "/*", fileSystem, recursive = true)
    assert(paths.size == 5, "should have ignored files starting with . or _")
  }

  it should "retrieve all files recursively with glob pattern receiving Hadoop conf" in {
    val paths = FileUtils.getFiles(baseTestDir.getCanonicalPath + "/*", new Configuration(), recursive = true)
    assert(paths.size == 5, "should have ignored files starting with . or _")
  }
}