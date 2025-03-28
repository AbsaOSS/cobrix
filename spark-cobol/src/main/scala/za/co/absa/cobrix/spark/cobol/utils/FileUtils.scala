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

import java.io.{FileOutputStream, OutputStreamWriter, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import za.co.absa.cobrix.cobol.internal.Logging

import scala.collection.JavaConverters._

/**
  * Retrieves files from a given file system.
  *
  * Supports glob patterns and recursive retrieval.
  *
  * Applies the same filter as Hadoop's FileInputFormat, which excludes files starting with '.' or '_'.
  */
object FileUtils extends Logging {

  val THRESHOLD_DIR_LENGTH_FOR_SINGLE_FILE_CHECK = 50

  private val hiddenFileFilter = new PathFilter() {
    def accept(p: Path): Boolean = {
      val name = p.getName
      !name.startsWith("_") && !name.startsWith(".")
    }
  }

  /**
    * Retrieves files from a directory, recursively or not.
    *
    * The directory may be informed through a glob pattern.
    */
  def getFiles(dir: String, hadoopConf: Configuration, recursive: Boolean = false): List[String] = {
    getFiles(dir, new Path(dir).getFileSystem(hadoopConf), recursive)
  }

  /**
    * Retrieves files from a directory, recursively or not.
    *
    * The directory may be informed through a glob pattern.
    */
  def getFiles(dir: String, fileSystem: FileSystem, recursive: Boolean): List[String] = {

    val dirPath = new Path(dir)
    val stats: Array[FileStatus] = fileSystem.globStatus(dirPath, hiddenFileFilter)

    if (stats == null) {
      throw new IllegalArgumentException(s"Input path does not exist: $dir")
    }

    val allFiles = stats.iterator.flatMap(stat => {
      if (stat.isDirectory) {
        if (recursive) {
          getAllFiles(stat.getPath, fileSystem)
        }
        else {
          fileSystem.listStatus(stat.getPath, hiddenFileFilter).filter(!_.isDirectory)
        }
      }
      else {
        List(stat)
      }
    })

    allFiles.map(_.getPath.toString).toList
  }

  /**
    * Writes a string to a file
    */
  def writeStringToFile(string: String, filePathName: String): Unit = {
    val writer = new PrintWriter(filePathName)
    try {
      writer.write(string)
    } finally {
      writer.close()
    }
  }

  /**
    * Writes a string to a file in UTF-8 encoding
    */
  def writeStringToUtf8File(string: String, filePathName: String): Unit = {
    val utf8Output: OutputStreamWriter  = new OutputStreamWriter(
      new FileOutputStream(filePathName),
      StandardCharsets.UTF_8
    )

    val writer = new PrintWriter(utf8Output)
    try {
      writer.write(string)
    } finally {
      writer.close()
    }
  }

  /**
    * Writes array of strings to a file
    */
  def writeStringsToFile(strings: Array[String], filePathName: String): Unit = {
    val writer = new PrintWriter(filePathName, "UTF-8")
    try {
      for (str <- strings) {
        writer.write(str)
        writer.write("\n")
      }
    } finally {
      writer.close()
    }
  }

  /**
    * Writes array of strings to a file in UTF-8 encoding
    */
  def writeStringsToUtf8File(strings: Array[String], filePathName: String): Unit = {
    val utf8Output: OutputStreamWriter  = new OutputStreamWriter(
      new FileOutputStream(filePathName),
      StandardCharsets.UTF_8
    )

    val writer = new PrintWriter(utf8Output)
    try {
      for (str <- strings) {
        writer.write(str)
        writer.write("\n")
      }
    } finally {
      writer.close()
    }
  }

  def readAllFileLines(fileName: String): String = {
    Files.readAllLines(Paths.get(fileName), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
  }

  def readAllFileStringUtf8(fileName: String): String = {
    Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8).toArray.mkString("\n")
  }

  def readAllFileLinesUtf8(fileName: String): Array[String] = {
    Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8).asScala.toArray
  }

  /**
    * Recursively retrieves all files from the directory tree.
    */
  private def getAllFiles(dir: Path, fileSystem: FileSystem): Seq[FileStatus] = {
    fileSystem.listStatus(dir, hiddenFileFilter).flatMap(stat => {
      if (stat.isDirectory) {
        getAllFiles(stat.getPath, fileSystem)
      }
      else {
        Seq(stat)
      }
    })
  }

  def getNumberOfFilesInDir(directory: String, fileSystem: FileSystem): Int = {
    val statuses = fileSystem.globStatus(new Path(directory), hiddenFileFilter)

    if (statuses == null) {
      throw new IllegalArgumentException(s"File or directory does not exist: $directory")
    }

    expandDirectories(fileSystem, statuses).length
  }

  /**
    * Finds the first file that is non-divisible by a given divisor and logs its name.
    */
  def findAndLogFirstNonDivisibleFile(sourceDir: String, divisor: Long, fileSystem: FileSystem): Option[(String, Long)] = {

    val allFiles = expandDirectories(fileSystem, fileSystem.globStatus(new Path(sourceDir), hiddenFileFilter))

    val firstNonDivisibleFile = allFiles.take(THRESHOLD_DIR_LENGTH_FOR_SINGLE_FILE_CHECK)
      .find(isNonDivisible(_, divisor))

    if (firstNonDivisibleFile.isDefined) {
      logger.error(s"File ${firstNonDivisibleFile.get.getPath} size (${firstNonDivisibleFile.get.getLen}) IS NOT divisible by $divisor.")
    }

    firstNonDivisibleFile.map(status => (status.getPath.toString, status.getLen))
  }

  /**
    * Finds all the files the are not divisible by a given divisor and logs their names.
    */
  def findAndLogAllNonDivisibleFiles(sourceDir: String, divisor: Long, fileSystem: FileSystem): Seq[(String, Long)] = {

    val allFiles = expandDirectories(fileSystem, fileSystem.globStatus(new Path(sourceDir), hiddenFileFilter))

    val allNonDivisibleFiles = allFiles.filter(isNonDivisible(_, divisor))

    if (allNonDivisibleFiles.nonEmpty) {
      allNonDivisibleFiles.foreach(file => logger.error(s"File ${file.getPath} size (${file.getLen}) IS NOT divisible by $divisor."))
    }

    allNonDivisibleFiles.map(status => (status.getPath.toString, status.getLen))
  }

  private def isNonDivisible(fileStatus: FileStatus, divisor: Long) = fileStatus.getLen % divisor != 0

  /**
    * Recursively expends directories listed in the incoming array of file statuses. But goes only 1 level deep
    * to match Spark's behavior on this.
    */
  private def expandDirectories(fileSystem: FileSystem, filesAndDirs: Array[FileStatus]): Array[FileStatus] = {
    filesAndDirs.flatMap(fileStatus => {
      if (fileStatus.isDirectory) {
        val newPath = new Path(fileStatus.getPath, "*")
        fileSystem.globStatus(newPath, hiddenFileFilter).filter(!_.isDirectory)
      } else {
        Array(fileStatus)
      }
    })
  }
}