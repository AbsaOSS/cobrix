package za.co.absa.cobrix.spark.cobol.mocks

import za.co.absa.cobrix.cobol.reader.extractors.raw.{RawRecordContext, RawRecordExtractor}

/**
  * This record extractor assumes each record has the size of 2 bytes.
  *
  * This record extractor is not index compatible.
  */
class FixedRecordExtractorNoIndex (ctx: RawRecordContext) extends Serializable with RawRecordExtractor {
  ctx.headerStream.close()

  private var currentOffset = ctx.inputStream.offset
  private var recordNumber = ctx.startingRecordNumber

  private var currentRecord = fetchRecord()

  // This record extractor does not support indexes because it returns offsets not pointing to the next record.
  // Since the record is fetched eagerly, it returns the offset of the next record.
  override def offset: Long = currentOffset

  override def hasNext: Boolean = currentRecord.nonEmpty

  @throws[NoSuchElementException]
  override def next(): Array[Byte] = {
    if (!hasNext) {
      throw new NoSuchElementException
    }

    val rawRecord = currentRecord.get

    // In order to support indexes the next 2 lines should be reversed.
    currentRecord = fetchRecord()
    currentOffset = ctx.inputStream.offset

    recordNumber += 1

    rawRecord
  }

  def fetchRecord(): Option[Array[Byte]] = {
    if (ctx.inputStream.isEndOfStream) {
      None
    } else {
      Option(ctx.inputStream.next(2))
    }
  }
}
