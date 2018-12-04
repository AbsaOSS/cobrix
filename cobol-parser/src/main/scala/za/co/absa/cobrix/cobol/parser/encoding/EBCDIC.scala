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

package za.co.absa.cobrix.cobol.parser.encoding

import za.co.absa.cobrix.cobol.parser.position.Position
import scodec.Codec
import scodec.codecs.{float, floatL, int16, int32, int64, int8, uint, uint16, uint32, uint4, uint8}

/**
  * EBCDIC() type abstraction
  *
  * @return : scodec [Codec] type that is used for encoding/decoding bit vectors to numbers and visa versa.
  */
case class EBCDIC() extends Encoding {
  def codec(comp: Option[Int], precision: Int, signPosition: Option[Position]): Codec[_ <: AnyVal] = {
    val cd = comp match {
      case Some(x) if x.isInstanceOf[Int] =>
        x match {
          case bin if bin == 0 || bin == 4 || bin == 5 =>
            precision match { //if native binary follow IBM guide to digit binary length
              case a if a >= 1 && a <= 2 && bin == 5 =>
                if (signPosition.getOrElse(None) != None) int8 else uint8
              case a if a >= 1 && a <= 4 =>
                if (signPosition.getOrElse(None) != None) int16 else uint16
              case b if b >= 5 && b <= 9 =>
                if (signPosition.getOrElse(None) != None) int32 else uint32
              case c if c >= 10 && c <= 18 =>
                if (signPosition.getOrElse(None) != None) int64 else int64
            }
          case spfloat if spfloat == 1 => float
          case dpfloat if dpfloat == 2 => floatL
          case bcd if bcd == 3 => uint4
        }
      case None => uint8 // DISPLAY(Every digit=byte), remember highest nybble of LSB contains the sign
    }
    cd
  }
}
