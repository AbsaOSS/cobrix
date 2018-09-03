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

package za.co.absa.cobrix.cobol.parser.common

import za.co.absa.cobrix.cobol.parser.stream.SimpleStream

/** This is  */
class SimpleMemoryStream(stream: SimpleStream, lookbackBufferSize: Int) {

  var smallestAvailableIndex: Long = -1
  var largestAvailableIndex: Long = -1
  val loopbackBuffer: Array[Byte] = new Array[Byte](lookbackBufferSize)

  /* Gets the specific number of bytes starting from specific index. Returns the number of bytes read */
  def getBytes(buffer: Array[Byte], startIndex: Long, endIndex: Long): Int = {
    if (startIndex < smallestAvailableIndex)
      throw new IllegalStateException("The index is too small")
    if (endIndex < startIndex)
      throw new IllegalStateException("Illegal indexes")

    if (largestAvailableIndex < 0) {
      val arr = stream.next(endIndex.toInt + 1)
      smallestAvailableIndex = 0
      largestAvailableIndex = endIndex
      var i = 0
      while (i <= endIndex) {
        loopbackBuffer(i) = arr(i)
        i += 1
      }
      i = 0
      while (i < endIndex - startIndex + 1) {
        buffer(i) = loopbackBuffer((startIndex + i).toInt)
        i += 1
      }
      return (endIndex - startIndex).toInt + 1
    }

    val additionalBytes = (endIndex - largestAvailableIndex).toInt
    if (additionalBytes > 0) {
      val arr = stream.next(additionalBytes)
      val actualBytes = arr.length

      var i = startIndex
      while (i <= endIndex && i <= largestAvailableIndex + actualBytes) {
        if (i <= largestAvailableIndex) {
          buffer((i - startIndex).toInt) = loopbackBuffer((i - smallestAvailableIndex).toInt)
        } else {
          val j = i - largestAvailableIndex - 1
          buffer((i - startIndex).toInt) = arr(j.toInt)
        }
        i += 1
      }
      if (largestAvailableIndex + smallestAvailableIndex + arr.length < lookbackBufferSize) {
        var j = 0
        while (j < arr.length) {
          loopbackBuffer((largestAvailableIndex - smallestAvailableIndex).toInt + j + 1) = arr(j)
          j += 1
        }
        largestAvailableIndex += arr.length
        (i - startIndex).toInt
      } else {
        val newLargestIndex = largestAvailableIndex + arr.length
        val newSmallestIndex = newLargestIndex - lookbackBufferSize + 1
        if (newLargestIndex!=largestAvailableIndex && newSmallestIndex!=smallestAvailableIndex) {
          var i = newSmallestIndex
          while (i <= newLargestIndex) {
            if (i <= largestAvailableIndex) {
              loopbackBuffer((i - newSmallestIndex).toInt) = loopbackBuffer((i - smallestAvailableIndex).toInt)
            } else {
              loopbackBuffer((i - newSmallestIndex).toInt) = arr((i - largestAvailableIndex - 1).toInt)
            }
            i += 1
          }
          smallestAvailableIndex = newSmallestIndex
          largestAvailableIndex = newLargestIndex
        }
        (Math.min(largestAvailableIndex, endIndex) - startIndex).toInt + 1
      }
    } else {
      var i = 0
      while (i <= endIndex - startIndex) {
        buffer(i) = loopbackBuffer((startIndex - smallestAvailableIndex + i).toInt)
        i += 1
      }
      (endIndex - startIndex).toInt + 1
    }
  }

  def search(searchBytes: Array[Byte], startIndex: Long): Long = {
    var state = 0
    if (startIndex < smallestAvailableIndex)
      throw new IllegalStateException("The index is too small")

    var bytesSearched = 0
    var i = startIndex
    while (state < searchBytes.length && i <= largestAvailableIndex) {
      if (loopbackBuffer( (i - smallestAvailableIndex).toInt ) == searchBytes(state))
        state += 1
      else
        state = 0
      i += 1
      bytesSearched += 1
    }

    if (state == searchBytes.length) {
      i - searchBytes.length
    } else {
      var bytesRead = 1
      val addsize = lookbackBufferSize/2
      val buf = new Array[Byte](addsize)
      while (state < searchBytes.length && bytesRead>0) {
        bytesRead = getBytes(buf, i, i + addsize - 1)
        if (bytesRead>0) {
          var j = 0
          while (state < searchBytes.length && j < bytesRead) {
            if (buf(j) == searchBytes(state))
              state += 1
            else
              state = 0
            i += 1
            j += 1
          }
        }
      }
      if (state == searchBytes.length)
        i - searchBytes.length
      else
        -1
    }
  }

}
