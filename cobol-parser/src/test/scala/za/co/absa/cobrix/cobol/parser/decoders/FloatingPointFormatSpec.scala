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

package za.co.absa.cobrix.cobol.parser.decoders

import org.scalatest.WordSpec

class FloatingPointFormatSpec extends WordSpec {

  import FloatingPointFormat._

  "FloatingPointFormat.withNameOpt()" should {
    "extract IBM (big endian) format" in {
      assert(withNameOpt("IBM").get == IBM)
      assert(withNameOpt("Ibm").get == IBM)
      assert(withNameOpt("ibm").get == IBM)
    }

    "extract IBM (little endian) format" in {
      assert(withNameOpt("ibm_little_endian").get == IBM_LE)
      assert(withNameOpt("ibm_LITTLE_endian").get == IBM_LE)
      assert(withNameOpt("ibm_little_Endian").get == IBM_LE)
    }

    "extract IEE754 (big endian) format" in {
      assert(withNameOpt("IEEE754").get == IEEE754)
      assert(withNameOpt("Ieee754").get == IEEE754)
      assert(withNameOpt("ieee754").get == IEEE754)
    }

    "extract IEEE754 (little endian) format" in {
      assert(withNameOpt("IEEE754_little_endian").get == IEEE754_LE)
      assert(withNameOpt("ieee754_Little_endian").get == IEEE754_LE)
      assert(withNameOpt("ieee754_little_endian").get == IEEE754_LE)
    }

    "return None for unknown formats" in {
      assert(withNameOpt("Unknown").isEmpty)
    }

  }


}
