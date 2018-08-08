package za.co.absa.cobrix.spark.cobol.reader.fixedlen

import org.apache.spark.sql.SparkSession

/**
  * Trait used to specify entities (e.g. DataSources) that know how to produce Readers.
  *
  * This would push the burden to created specialized Readers into specialized DataSources, "decentralizing" the implementation of new sources.
  * If, at any time, this decentralization becomes undesired, the model can be easily redone as a Factory Method (GoF).
  */
trait FixedLenReaderFactory {
  def buildReader(spark: SparkSession, parameters: Map[String, String]): FixedLenReader
}
