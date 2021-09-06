package za.co.absa.cobrix.cobol.reader.extractors.raw

import za.co.absa.cobrix.cobol.parser.{Copybook, CopybookParser}
import za.co.absa.cobrix.cobol.reader.memorystream.TestStringStream
import za.co.absa.cobrix.cobol.reader.recordheader.{RecordHeaderDecoder, RecordHeaderDecoderBdw, RecordHeaderDecoderRdw, RecordHeaderParameters}
import za.co.absa.cobrix.cobol.reader.stream.SimpleStream

object RawRecordContextFactory {
  private val copybookContent =
    """      01 RECORD.
          02 X PIC X(1).
    """
  private val copybook = CopybookParser.parseTree(copybookContent)

  def getDummyRawRecordContext(
                                startingRecordNumber: Long = 0L,
                                inputStream: SimpleStream = new TestStringStream("A1\nB2\n"),
                                copybook: Copybook = copybook,
                                rdwDecoder: RecordHeaderDecoder = new RecordHeaderDecoderBdw(RecordHeaderParameters(isBigEndian = false, 0)),
                                bdwDecoder: RecordHeaderDecoder = new RecordHeaderDecoderRdw(RecordHeaderParameters(isBigEndian = false, 0)),
                                additionalInfo: String = ""
                              ): RawRecordContext = {
    RawRecordContext(startingRecordNumber, inputStream, copybook, rdwDecoder, bdwDecoder, additionalInfo)
  }

}
