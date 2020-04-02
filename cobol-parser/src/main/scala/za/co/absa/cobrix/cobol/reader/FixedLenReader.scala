package za.co.absa.cobrix.cobol.reader

/** The abstract class for Cobol block (fixed length records) data readers from various sources */
trait FixedLenReader extends Reader with Serializable {
  @throws(classOf[Exception]) protected def getRecordIterator(binaryData: Array[Byte]): Iterator[Seq[Any]]
}
