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

package za.co.absa.cobrix.cobol.parser.encoding.codepage

/**
  * EBCDIC code page 1144. Italy.
  *
  * It corresponds to code page 280 and only differs from it in position 9F, where the euro sign € is located instead
  * of the international currency symbol ¤.
  */
class CodePage1144 extends SingleByteCodePage(CodePage1144.ebcdicToAsciiMapping) {
  override def codePageShortName: String = "cp1144"
}

object CodePage1144 {
  val ebcdicToAsciiMapping: Array[Char] = {
    import EbcdicNonPrintable._

    /* This is the EBCDIC Code Page 1144 to ASCII conversion table
       from https://en.wikibooks.org/wiki/Character_Encodings/Code_Tables/EBCDIC/EBCDIC_280 */
    val ebcdic2ascii: Array[Char] = {
      // Non-printable characters map used: http://www.pacsys.com/asciitab.htm
      Array[Char](
        c00, c01, c02, c03, spc, c09, spc, del, spc, spc, spc, c0b, c0c, ccr, c0e, c0f, //   0 -  15
        c10, c11, c12, c13, spc, nel, c08, spc, c18, c19, spc, spc, c1c, c1d, c1e, c1f, //  16 -  31
        spc, spc, spc, spc, spc, clf, c17, c1b, spc, spc, spc, spc, spc, c05, c06, c07, //  32 -  47
        spc, spc, c16, spc, spc, spc, spc, c04, spc, spc, spc, spc, c14, c15, spc, c1a, //  48 -  63
        ' ', rsp, 'â', 'ä', '{', 'á', 'ã', 'å', bsh, 'ñ', '°', '.', '<', '(', '+', '!', //  64 -  79
        '&', ']', 'ê', 'ë', '}', 'í', 'î', 'ï', '~', 'ß', 'é', '$', '*', ')', ';', '^', //  80 -  95
        '-', '/', 'Â', 'Ä', 'À', 'Á', 'Ã', 'Å', 'Ç', 'Ñ', 'ò', ',', '%', '_', '>', '?', //  96 - 111
        'ø', 'É', 'Ê', 'Ë', 'È', 'Í', 'Î', 'Ï', 'Ì', 'ù', ':', '£', '§', qts, '=', qtd, // 112 - 127
        'Ø', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '«', '»', 'ð', 'ý', 'þ', '±', // 128 - 143
        '[', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 'ª', 'º', 'æ', '¸', 'Æ', '€', // 144 - 159
        'µ', 'ì', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '¡', '¿', 'Ð', 'Ý', 'Þ', '®', // 160 - 175
        '¢', '#', '¥', '·', '©', '@', '¶', '¼', '½', '¾', '¬', '|', '¯', '¨', '´', '×', // 176 - 191
        'à', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', shy, 'ô', 'ö', '¦', 'ó', 'õ', // 192 - 207
        'è', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', '¹', 'û', 'ü', '`', 'ú', 'ÿ', // 208 - 223
        'ç', '÷', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '²', 'Ô', 'Ö', 'Ò', 'Ó', 'Õ', // 224 - 239
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '³', 'Û', 'Ü', 'Ù', 'Ú', spc) // 240 - 255
    }
    ebcdic2ascii

  }

  /**
   * To generate conversion mapping use the python script shared in the PR.
   */
  val asciiToEbcdicMapping: Int => Byte = {
    case 0 => 0x00.toByte
    case 1 => 0x01.toByte
    case 2 => 0x02.toByte
    case 3 => 0x03.toByte
    case 156 => 0x04.toByte
    case 9 => 0x05.toByte
    case 134 => 0x06.toByte
    case 127 => 0x07.toByte
    case 151 => 0x08.toByte
    case 141 => 0x09.toByte
    case 142 => 0x0a.toByte
    case 11 => 0x0b.toByte
    case 12 => 0x0c.toByte
    case 13 => 0x0d.toByte
    case 14 => 0x0e.toByte
    case 15 => 0x0f.toByte
    case 16 => 0x10.toByte
    case 17 => 0x11.toByte
    case 18 => 0x12.toByte
    case 19 => 0x13.toByte
    case 157 => 0x14.toByte
    case 8 => 0x16.toByte
    case 135 => 0x17.toByte
    case 24 => 0x18.toByte
    case 25 => 0x19.toByte
    case 146 => 0x1a.toByte
    case 143 => 0x1b.toByte
    case 28 => 0x1c.toByte
    case 29 => 0x1d.toByte
    case 30 => 0x1e.toByte
    case 31 => 0x1f.toByte
    case 128 => 0x20.toByte
    case 129 => 0x21.toByte
    case 130 => 0x22.toByte
    case 131 => 0x23.toByte
    case 132 => 0x24.toByte
    case 10 => 0x25.toByte //NL and LF EBCDIC representation map to LF in Unicode. Choosing to always map LF to LF
    case 23 => 0x26.toByte
    case 27 => 0x27.toByte
    case 136 => 0x28.toByte
    case 137 => 0x29.toByte
    case 138 => 0x2a.toByte
    case 139 => 0x2b.toByte
    case 140 => 0x2c.toByte
    case 5 => 0x2d.toByte
    case 6 => 0x2e.toByte
    case 7 => 0x2f.toByte
    case 144 => 0x30.toByte
    case 145 => 0x31.toByte
    case 22 => 0x32.toByte
    case 147 => 0x33.toByte
    case 148 => 0x34.toByte
    case 149 => 0x35.toByte
    case 150 => 0x36.toByte
    case 4 => 0x37.toByte
    case 152 => 0x38.toByte
    case 153 => 0x39.toByte
    case 154 => 0x3a.toByte
    case 155 => 0x3b.toByte
    case 20 => 0x3c.toByte
    case 21 => 0x3d.toByte
    case 158 => 0x3e.toByte
    case 26 => 0x3f.toByte
    case 32 => 0x40.toByte
    case 160 => 0x41.toByte
    case 226 => 0x42.toByte
    case 228 => 0x43.toByte
    case 123 => 0x44.toByte
    case 225 => 0x45.toByte
    case 227 => 0x46.toByte
    case 229 => 0x47.toByte
    case 92 => 0x48.toByte
    case 241 => 0x49.toByte
    case 176 => 0x4a.toByte
    case 46 => 0x4b.toByte
    case 60 => 0x4c.toByte
    case 40 => 0x4d.toByte
    case 43 => 0x4e.toByte
    case 33 => 0x4f.toByte
    case 38 => 0x50.toByte
    case 93 => 0x51.toByte
    case 234 => 0x52.toByte
    case 235 => 0x53.toByte
    case 125 => 0x54.toByte
    case 237 => 0x55.toByte
    case 238 => 0x56.toByte
    case 239 => 0x57.toByte
    case 126 => 0x58.toByte
    case 223 => 0x59.toByte
    case 233 => 0x5a.toByte
    case 36 => 0x5b.toByte
    case 42 => 0x5c.toByte
    case 41 => 0x5d.toByte
    case 59 => 0x5e.toByte
    case 94 => 0x5f.toByte
    case 45 => 0x60.toByte
    case 47 => 0x61.toByte
    case 194 => 0x62.toByte
    case 196 => 0x63.toByte
    case 192 => 0x64.toByte
    case 193 => 0x65.toByte
    case 195 => 0x66.toByte
    case 197 => 0x67.toByte
    case 199 => 0x68.toByte
    case 209 => 0x69.toByte
    case 242 => 0x6a.toByte
    case 44 => 0x6b.toByte
    case 37 => 0x6c.toByte
    case 95 => 0x6d.toByte
    case 62 => 0x6e.toByte
    case 63 => 0x6f.toByte
    case 248 => 0x70.toByte
    case 201 => 0x71.toByte
    case 202 => 0x72.toByte
    case 203 => 0x73.toByte
    case 200 => 0x74.toByte
    case 205 => 0x75.toByte
    case 206 => 0x76.toByte
    case 207 => 0x77.toByte
    case 204 => 0x78.toByte
    case 249 => 0x79.toByte
    case 58 => 0x7a.toByte
    case 163 => 0x7b.toByte
    case 167 => 0x7c.toByte
    case 39 => 0x7d.toByte
    case 61 => 0x7e.toByte
    case 34 => 0x7f.toByte
    case 216 => 0x80.toByte
    case 97 => 0x81.toByte
    case 98 => 0x82.toByte
    case 99 => 0x83.toByte
    case 100 => 0x84.toByte
    case 101 => 0x85.toByte
    case 102 => 0x86.toByte
    case 103 => 0x87.toByte
    case 104 => 0x88.toByte
    case 105 => 0x89.toByte
    case 171 => 0x8a.toByte
    case 187 => 0x8b.toByte
    case 240 => 0x8c.toByte
    case 253 => 0x8d.toByte
    case 254 => 0x8e.toByte
    case 177 => 0x8f.toByte
    case 91 => 0x90.toByte
    case 106 => 0x91.toByte
    case 107 => 0x92.toByte
    case 108 => 0x93.toByte
    case 109 => 0x94.toByte
    case 110 => 0x95.toByte
    case 111 => 0x96.toByte
    case 112 => 0x97.toByte
    case 113 => 0x98.toByte
    case 114 => 0x99.toByte
    case 170 => 0x9a.toByte
    case 186 => 0x9b.toByte
    case 230 => 0x9c.toByte
    case 184 => 0x9d.toByte
    case 198 => 0x9e.toByte
    case 8364 => 0x9f.toByte
    case 181 => 0xa0.toByte
    case 236 => 0xa1.toByte
    case 115 => 0xa2.toByte
    case 116 => 0xa3.toByte
    case 117 => 0xa4.toByte
    case 118 => 0xa5.toByte
    case 119 => 0xa6.toByte
    case 120 => 0xa7.toByte
    case 121 => 0xa8.toByte
    case 122 => 0xa9.toByte
    case 161 => 0xaa.toByte
    case 191 => 0xab.toByte
    case 208 => 0xac.toByte
    case 221 => 0xad.toByte
    case 222 => 0xae.toByte
    case 174 => 0xaf.toByte
    case 162 => 0xb0.toByte
    case 35 => 0xb1.toByte
    case 165 => 0xb2.toByte
    case 183 => 0xb3.toByte
    case 169 => 0xb4.toByte
    case 64 => 0xb5.toByte
    case 182 => 0xb6.toByte
    case 188 => 0xb7.toByte
    case 189 => 0xb8.toByte
    case 190 => 0xb9.toByte
    case 172 => 0xba.toByte
    case 124 => 0xbb.toByte
    case 175 => 0xbc.toByte
    case 168 => 0xbd.toByte
    case 180 => 0xbe.toByte
    case 215 => 0xbf.toByte
    case 224 => 0xc0.toByte
    case 65 => 0xc1.toByte
    case 66 => 0xc2.toByte
    case 67 => 0xc3.toByte
    case 68 => 0xc4.toByte
    case 69 => 0xc5.toByte
    case 70 => 0xc6.toByte
    case 71 => 0xc7.toByte
    case 72 => 0xc8.toByte
    case 73 => 0xc9.toByte
    case 173 => 0xca.toByte
    case 244 => 0xcb.toByte
    case 246 => 0xcc.toByte
    case 166 => 0xcd.toByte
    case 243 => 0xce.toByte
    case 245 => 0xcf.toByte
    case 232 => 0xd0.toByte
    case 74 => 0xd1.toByte
    case 75 => 0xd2.toByte
    case 76 => 0xd3.toByte
    case 77 => 0xd4.toByte
    case 78 => 0xd5.toByte
    case 79 => 0xd6.toByte
    case 80 => 0xd7.toByte
    case 81 => 0xd8.toByte
    case 82 => 0xd9.toByte
    case 185 => 0xda.toByte
    case 251 => 0xdb.toByte
    case 252 => 0xdc.toByte
    case 96 => 0xdd.toByte
    case 250 => 0xde.toByte
    case 255 => 0xdf.toByte
    case 231 => 0xe0.toByte
    case 247 => 0xe1.toByte
    case 83 => 0xe2.toByte
    case 84 => 0xe3.toByte
    case 85 => 0xe4.toByte
    case 86 => 0xe5.toByte
    case 87 => 0xe6.toByte
    case 88 => 0xe7.toByte
    case 89 => 0xe8.toByte
    case 90 => 0xe9.toByte
    case 178 => 0xea.toByte
    case 212 => 0xeb.toByte
    case 214 => 0xec.toByte
    case 210 => 0xed.toByte
    case 211 => 0xee.toByte
    case 213 => 0xef.toByte
    case 48 => 0xf0.toByte
    case 49 => 0xf1.toByte
    case 50 => 0xf2.toByte
    case 51 => 0xf3.toByte
    case 52 => 0xf4.toByte
    case 53 => 0xf5.toByte
    case 54 => 0xf6.toByte
    case 55 => 0xf7.toByte
    case 56 => 0xf8.toByte
    case 57 => 0xf9.toByte
    case 179 => 0xfa.toByte
    case 219 => 0xfb.toByte
    case 220 => 0xfc.toByte
    case 217 => 0xfd.toByte
    case 218 => 0xfe.toByte
    case 159 => 0xff.toByte
    case _ => 0x40.toByte // defaults to space if mapping not available.
  }
}
