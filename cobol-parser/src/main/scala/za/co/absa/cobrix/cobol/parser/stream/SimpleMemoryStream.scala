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

package za.co.absa.cobrix.cobol.parser.stream

/**
  * This is a stream with a lookback buffer. It allows you to read bytes from it and sometime go back and fetch previous bytes.
  * It also supports searching for a specific sequence of bytes.
  *
  * The pattern of usage for this stream is this.
  * <ul>
  * <li>Create an instance of [[za.co.absa.cobrix.cobol.parser.stream.SimpleStream]].</li>
  * <li>Create an instance of [[za.co.absa.cobrix.cobol.parser.stream.SimpleMemoryStream]] and pass the lookback buffer size and
  *   the object created at the first step.</li>
  * </ul>
  * <li>Search a sequence of bytes in the stream.</li>
  * <li>Once the sequence of bytes is found you can fetch bytes before the sequence and after it by providing the specific indexes.</li>
  * This memory stream provides getBytes interface which accepts indexes. The stream will be read sequentially and once values from bigger
  * indexes are read you can look back only limited number of bytes.
  *
  * @param stream An instance of [[za.co.absa.cobrix.cobol.parser.stream.SimpleStream]] as the source of bytes.
  * @param lookbackBufferSize The number of bytes to keep in the look back buffer.
  *
  * */
class SimpleMemoryStream(stream: SimpleStream, lookbackBufferSize: Int) {

  var smallestAvailableIndex: Long = -1
  var largestAvailableIndex: Long = -1
  val loopbackBuffer: Array[Byte] = new Array[Byte](lookbackBufferSize)

  /* Gets the specific number of bytes starting from specific index. Returns the number of bytes read */
  def getBytes(buffer: Array[Byte], startIndex: Long, endIndex: Long): Int = {
    if (startIndex < smallestAvailableIndex)
      throw new IllegalStateException("The index is too small: $startIndex < $smallestAvailableIndex. Please increase the lookback buffer size.")
    if (endIndex < startIndex)
      throw new IllegalStateException(s"Illegal indexes passed: $endIndex < $startIndex.")

    if (largestAvailableIndex < 0) {
      initialExtractFromStream(buffer, startIndex, endIndex)
    } else {
      val additionalBytes = (endIndex - largestAvailableIndex).toInt
      if (additionalBytes > 0) {
        extractOutOfLoobackBufferBytes(buffer, startIndex, endIndex, additionalBytes)
      } else {
        extractBytesFromLookbackBuffer(buffer, startIndex, endIndex)
      }
    }
  }

  /** Searches the stream for the specified sequence of bytes. Returns the index of the beginning of the found sequence, -1 if not found. */
  def search(searchBytes: Array[Byte], startIndex: Long): Long = {
    var state = 0
    if (startIndex < smallestAvailableIndex)
      throw new IllegalStateException("The index is too small: $startIndex < $smallestAvailableIndex. Please increase the lookback buffer size.")

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
      searchOutOfLookbackBuffer(searchBytes, state)
    }
  }

  /** Searches the sequence of bytes outsize of the lookback buffer. Returns the index of the beginning of the found sequence, -1 if not found. */
  def searchOutOfLookbackBuffer(searchBytes: Array[Byte], initialSearchState: Int): Long = {
    var state = initialSearchState
    var bytesRead = 1
    val addsize = lookbackBufferSize / 2
    val buf = new Array[Byte](addsize)
    var i = largestAvailableIndex + 1
    while (state < searchBytes.length && bytesRead > 0) {
      bytesRead = getBytes(buf, i, i + addsize - 1)
      if (bytesRead > 0) {
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

  /** Extracts the initial bytes from the stream when the lookback buffer is empty */
  private def initialExtractFromStream(buffer: Array[Byte], startIndex: Long, endIndex: Long): Int = {
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
    (endIndex - startIndex).toInt + 1
  }

  /** Extracts the values ahead of the lookback buffer and shifts the lookback buffer accordingly */
  private def extractOutOfLoobackBufferBytes(buffer: Array[Byte], startIndex: Long, endIndex: Long, additionalBytes: Int) = {
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
    AddBytesToLookbackBuffer(arr)

    (Math.min(largestAvailableIndex, endIndex) - startIndex).toInt + 1
  }

  /** Adds bytes to the lookback buffer */
  private def AddBytesToLookbackBuffer(arr: Array[Byte]): Unit = {
    if (largestAvailableIndex + smallestAvailableIndex + arr.length < lookbackBufferSize) {
      var j = 0
      while (j < arr.length) {
        loopbackBuffer((largestAvailableIndex - smallestAvailableIndex).toInt + j + 1) = arr(j)
        j += 1
      }
      largestAvailableIndex += arr.length
    } else {
      val newLargestIndex = largestAvailableIndex + arr.length
      val newSmallestIndex = newLargestIndex - lookbackBufferSize + 1
      if (newLargestIndex != largestAvailableIndex && newSmallestIndex != smallestAvailableIndex) {
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
    }
  }

  /** Extracts bytes from the lookback buffer */
  private def extractBytesFromLookbackBuffer(buffer: Array[Byte], startIndex: Long, endIndex: Long) = {
    var i = 0
    while (i <= endIndex - startIndex) {
      buffer(i) = loopbackBuffer((startIndex - smallestAvailableIndex + i).toInt)
      i += 1
    }
    (endIndex - startIndex).toInt + 1
  }

}
