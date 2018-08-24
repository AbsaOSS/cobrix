package za.co.absa.cobrix.spark.cobol.schema

import za.co.absa.cobrix.spark.cobol.schema

object SchemaRetentionPolicy extends Enumeration {
  type SchemaRetentionPolicy = Value

  val KeepOriginal, CollapseRoot = Value

  def withNameOpt(s: String): Option[Value] = {
    val exactNames = values.find(_.toString == s)
    if (exactNames.isEmpty) {
      val sLowerCase = s.toLowerCase()
      if (sLowerCase == "keep_original") {
        Some(KeepOriginal)
      } else if (sLowerCase == "collapse_root") {
        Some(CollapseRoot)
      } else {
        None
      }
    } else {
      exactNames
    }
  }

}
