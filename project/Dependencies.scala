import sbt._

object Dependencies {

  private val sparkVersion = "2.2.2"

  private val guavaVersion = "15.0"
  private val scodecBitsVersion = "1.1.4"
  private val scodecCoreVersion = "1.10.3"
  private val antlrValue = "4.7.2"
  private val slf4jVersion = "1.7.25"

  private val scalatestVersion = "3.0.1"

  val SparkCobolDependencies: Seq[ModuleID] = Seq(
    // provided
    "org.apache.spark" %% "spark-core"       % sparkVersion % Provided,
    "org.apache.spark" %% "spark-sql"        % sparkVersion % Provided,
    "org.apache.spark" %% "spark-streaming"  % sparkVersion % Provided,

    // test
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
  )

  val SparkCobolDependenciesOverride: Seq[ModuleID] = Seq(
    // Needs to be added as a separate dependency since Spark uses an newer
    // version of Guava which has removed 'com.google.common.base.Stopwatch.elapsedMillis',
    // however, the version of Hadoop imported by Spark relies on that method.
    "com.google.guava" % "guava" % guavaVersion
  )
  val CobolParserDependencies: Seq[ModuleID] = Seq(
    // compile
    "org.scodec" %% "scodec-bits"    % scodecBitsVersion,
    "org.scodec" %% "scodec-core"    % scodecCoreVersion,
    "org.antlr"   % "antlr4-runtime" % antlrValue,
    "org.slf4j"   % "slf4j-api"      % slf4jVersion,

    // test
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
  )

}