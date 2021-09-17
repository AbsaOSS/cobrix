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

import Dependencies._
import BuildInfoTemplateSettings._

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.12"

ThisBuild / organization := "za.co.absa.cobrix"

ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala211, scala212)

ThisBuild / Test / javaOptions += "-Xmx2G"

ThisBuild / scalacOptions := Seq("-unchecked", "-deprecation")

// Scala shouldn't be packaged so it is explicitly added as a provided dependency below
ThisBuild / autoScalaLibrary := false

lazy val printSparkVersion = taskKey[Unit]("Print Spark version spark-cobol is building against.")

lazy val cobrix = (project in file("."))
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .settings(
    name := "cobrix",

    // No need to publish the aggregation [empty] artifact
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .aggregate(cobolParser, cobolConverters, sparkCobol)

lazy val cobolParser = (project in file("cobol-parser"))
  .settings(
    name := "cobol-parser",
    libraryDependencies ++= CobolParserDependencies :+ getScalaDependency(scalaVersion.value),
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    assemblySettings
  ).enablePlugins(AutomateHeaderPlugin)

lazy val cobolConverters = (project in file("cobol-converters"))
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .settings(
      name := "cobol-converters",
      libraryDependencies ++= CobolConvertersDependencies :+ getScalaDependency(scalaVersion.value),
      releasePublishArtifactsAction := PgpKeys.publishSigned.value
  ).dependsOn(cobolParser)
  .enablePlugins(AutomateHeaderPlugin)

lazy val sparkCobol = (project in file("spark-cobol"))
  .settings(
    name := "spark-cobol",
    printSparkVersion := {
      val log = streams.value.log
      log.info(s"Building with Spark ${sparkVersion(scalaVersion.value)}, Scala ${scalaVersion.value}")
      sparkVersion(scalaVersion.value)
    },
    (Compile / compile) := ((Compile / compile) dependsOn printSparkVersion).value,
    libraryDependencies ++= SparkCobolDependencies(scalaVersion.value) :+ getScalaDependency(scalaVersion.value),
    dependencyOverrides ++= SparkCobolDependenciesOverride,
    Test / fork := true, // Spark tests fail randomly otherwise
    populateBuildInfoTemplate,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    assemblySettings
  )
  .dependsOn(cobolParser)
  .enablePlugins(AutomateHeaderPlugin)

// scoverage settings
ThisBuild / coverageExcludedPackages := ".*examples.*;.*replication.*"
ThisBuild / coverageExcludedFiles := ".*Example.*;Test.*"

// release settings
releaseCrossBuild := true
addCommandAlias("releaseNow", ";set releaseVersionBump := sbtrelease.Version.Bump.Bugfix; release with-defaults")

lazy val assemblySettings = Seq(
  // This merge strategy retains service entries for all services in manifest.
  // It allows custom Spark data sources to be used together, e.g. 'spark-xml' and 'spark-cobol'.
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) =>
      xs map {_.toLowerCase} match {
        case "manifest.mf" :: Nil =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "maven" :: x =>
          MergeStrategy.discard
        case "services" :: x =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.deduplicate
      }
    case _ => MergeStrategy.deduplicate
  },
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
  assemblyShadeRules in assembly := Seq(
    // Spark may rely on a different version of ANTLR runtime. Renaming the package helps avoid the binary incompatibility
    ShadeRule.rename("org.antlr.**" -> "za.co.absa.cobrix.shaded.org.antlr.@1").inAll,
    // The SLF4j API and implementation are provided by Spark
    ShadeRule.zap("org.slf4j.**").inAll
  ),
  logLevel in assembly := Level.Info,
  test in assembly := {}
)
