package za.co.absa.cobrix.cobol.reader.recordheader

object RecordHeaderParametersFactory {
  def getDummyRecordHeaderParameters(isBigEndian: Boolean = false,
                                     adjustment: Int = 0): RecordHeaderParameters = {
    RecordHeaderParameters(isBigEndian, adjustment)
  }
}
