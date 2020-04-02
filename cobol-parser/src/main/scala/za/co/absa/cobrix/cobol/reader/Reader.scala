package za.co.absa.cobrix.cobol.reader

import za.co.absa.cobrix.cobol.reader.schema.CobolSchema

/** The abstract class for Cobol all data readers from various sources */
trait Reader extends Serializable {
  type Field = String
  type Value = String

  def getCobolSchema: CobolSchema

  def getRecordStartOffset: Int

  def getRecordEndOffset: Int
}
