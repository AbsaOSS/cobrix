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

import java.io.ByteArrayOutputStream

import org.apache.spark.sql.DataFrame

object TestUtils {

  /** Same as df.show(), bu the output goes to a string */
  def showString(df: DataFrame, numRows: Int = 20): String = {
    val outCapture = new ByteArrayOutputStream
    Console.withOut(outCapture) {
      df.show(numRows, truncate = false)
    }
    new String(outCapture.toByteArray).replace("\r\n", "\n")
  }

}
