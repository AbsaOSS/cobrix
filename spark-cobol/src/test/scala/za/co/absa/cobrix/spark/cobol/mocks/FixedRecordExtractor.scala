package za.co.absa.cobrix.spark.cobol.mocks

import za.co.absa.cobrix.cobol.reader.extractors.raw.{RawRecordContext, RawRecordExtractor}

/**
  * This record extractor assumes each record has the size of 2 bytes.
  */
class FixedRecordExtractor(ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  ctx.headerStream.close()

  private var recordNumber = ctx.startingRecordNumber

  override def offset: Long = ctx.inputStream.offset

  override def hasNext: Boolean = !ctx.inputStream.isEndOfStream

  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }

    val rawRecord = ctx.inputStream.next(2)

    recordNumber += 1

    rawRecord
  }

}
