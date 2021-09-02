package za.co.absa.cobrix.cobol.parser.recordformats

sealed trait RecordFormat

object RecordFormat {
  case object FixedLength extends RecordFormat
  case object VariableLength extends RecordFormat
  case object VariableBlock extends RecordFormat
  case object AsciiText extends RecordFormat

  def withNameOpt(s: String): Option[RecordFormat] = {
    s match {
      case "F" => Some(FixedLength)
      case "V" => Some(VariableLength)
      case "VB" => Some(VariableBlock)
      case "D" => Some(AsciiText)
      case _ => None
    }
  }
}
