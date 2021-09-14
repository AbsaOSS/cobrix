package za.co.absa.cobrix.cobol.reader.parameters

case class Bdw(
                isBigEndian: Boolean,
                adjustment: Int,
                blockLength: Option[Int],
                recordsPerBlock: Option[Int],
              )
