/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._

object Dependencies {

  private val sparkVersion = "2.4.4"

  private val guavaVersion = "15.0"
  private val scodecBitsVersion = "1.1.4"
  private val scodecCoreVersion = "1.10.3"
  private val antlrValue = "4.7.2"
  private val slf4jVersion = "1.7.25"

  private val scalatestVersion = "3.0.1"

  def getScalaDependency(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-library" % scalaVersion % Provided

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