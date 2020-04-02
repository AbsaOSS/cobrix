package za.co.absa.cobrix.cobol.reader.extractors.record

import za.co.absa.cobrix.cobol.parser.ast.Group


trait RecordHandler[T] {
  def create(values: Array[Any], group: Group): T
  def toSeq(record: T): Seq[Any]
}