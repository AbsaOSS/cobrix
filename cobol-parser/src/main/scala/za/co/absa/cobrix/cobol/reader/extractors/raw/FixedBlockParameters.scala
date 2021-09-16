package za.co.absa.cobrix.cobol.reader.extractors.raw

case class FixedBlockParameters(
                               recordLength: Option[Int],
                               blockLength: Option[Int],
                               recordsPerBlock: Option[Int]
                               )

object FixedBlockParameters {
  def validate(params: FixedBlockParameters): Unit = {
    if (params.blockLength.isEmpty && params.recordsPerBlock.isEmpty) {
      throw new IllegalArgumentException("FB record format requires block length or number records per block to be specified.")
    }
    if (params.blockLength.nonEmpty && params.recordsPerBlock.nonEmpty) {
      throw new IllegalArgumentException("FB record format requires either block length or number records per block to be specified, but not both.")
    }
    params.recordLength.foreach(x => if (x < 1) throw new IllegalArgumentException(s"Record length should be positive. Got $x."))
    params.blockLength.foreach(x => if (x < 1) throw new IllegalArgumentException(s"Block length should be positive. Got $x."))
    params.recordsPerBlock.foreach(x => if (x < 1) throw new IllegalArgumentException(s"Records per block should be positive. Got $x."))
  }
}