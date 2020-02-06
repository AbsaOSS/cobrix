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

ThisBuild / organization := "com.example"
ThisBuild / name         := "spark-cobol-app"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.11.12"

val sparkVersion = "2.4.4"
val sparkCobolVersion = "2.0.3"
val scalatestVersion = "3.0.1"

ThisBuild / libraryDependencies ++= Seq(
  "za.co.absa.cobrix" %% "spark-cobol"     % sparkCobolVersion,
  "org.scalatest"     %% "scalatest"       % scalatestVersion   % Test,
  "org.apache.spark"  %% "spark-core"      % sparkVersion       % Provided,
  "org.apache.spark"  %% "spark-sql"       % sparkVersion       % Provided,
  "org.apache.spark"  %% "spark-streaming" % sparkVersion       % Provided
)

// Do not run tests in parallel
parallelExecution in Test := false

// Do not run tests on assembly
test in assembly := {}

// Do not include Scala in the fat jar
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// This merge strategy retains service entries for all services in manifest.
// It allows custom Spark data sources to be used together, e.g. 'spark-xml' and 'spark-cobol'.
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    xs map {_.toLowerCase} match {
      case "manifest.mf" :: Nil =>
        MergeStrategy.discard
      case "services" :: x =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ => MergeStrategy.deduplicate
}
