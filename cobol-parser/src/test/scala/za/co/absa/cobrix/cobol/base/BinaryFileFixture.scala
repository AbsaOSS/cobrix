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

package za.co.absa.cobrix.cobol.base

import java.nio.file.{Files, Path, Paths}
import java.util.Comparator
import java.util.function.Consumer

/**
  * This fixture adds ability for a unit test to create temporary files for using them in the tests.
  */
trait BinaryFileFixture {
  def withTempDirectory(prefix: String)(f: String => Unit): Unit = {
    val tmpPath = Files.createTempDirectory(prefix)
    val pathStr = tmpPath.toAbsolutePath.toString

    f(pathStr)

    Files.walk(tmpPath)
      .sorted(Comparator.reverseOrder())
      .forEach(new Consumer[Path] {
        override def accept(f: Path): Unit = Files.delete(f)
      })
  }

  def writeBinaryFile(filePath: String, content: Array[Byte]): Unit = {
    Files.write(Paths.get(filePath), content)
  }

  def readBinaryFile(filePath: String): Array[Byte] = {
    Files.readAllBytes(Paths.get(filePath))
  }

  private def hex2bytes(hex: String): Array[Byte] = {
    val compactStr = hex.replaceAll("\\s", "")
    compactStr.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }
}
