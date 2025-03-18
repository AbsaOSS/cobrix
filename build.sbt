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
import ScalacOptions._
import com.github.sbt.jacoco.report.JacocoReportSettings

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.20"
lazy val scala213 = "2.13.15"

ThisBuild / organization := "za.co.absa.cobrix"

ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := Seq(scala211, scala212, scala213)

ThisBuild / Test / javaOptions += "-Xmx2G"

ThisBuild / javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

ThisBuild / scalacOptions := scalacOptionsFor(scalaVersion.value)

// Scala shouldn't be packaged so it is explicitly added as a provided dependency below
ThisBuild / autoScalaLibrary := false

lazy val printSparkVersion = taskKey[Unit]("Print Spark version spark-cobol is building against.")

lazy val commonJacocoReportSettings: JacocoReportSettings = JacocoReportSettings(
  formats = Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML)
)

lazy val commonJacocoExcludes: Seq[String] = Seq(
  "za.co.absa.cobrix.cobol.reader.parameters.*",                     // case classes only
  "za.co.absa.cobrix.cobol.reader.extractors.raw.RawRecordContext*"  // a case class
)

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
  .enablePlugins(ShadingPlugin)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "cobol-parser",
    libraryDependencies ++= CobolParserDependencies :+ getScalaDependency(scalaVersion.value),
    shadedDependencies ++= CobolParserShadedDependencies,
    shadingRules ++= Seq (
      ShadingRule.moveUnder("org.antlr.v4.runtime", "za.co.absa.cobrix.cobol.parser.shaded")
    ),
    validNamespaces ++= Set("za"),
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    assemblySettings,
    jacocoReportSettings := commonJacocoReportSettings.withTitle("cobrix:cobol-parser Jacoco Report"),
    jacocoExcludes := commonJacocoExcludes
  )

lazy val cobolConverters = (project in file("cobol-converters"))
  .dependsOn(cobolParser)
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "cobol-converters",
    libraryDependencies ++= CobolConvertersDependencies :+ getScalaDependency(scalaVersion.value),
    // No need to publish this artifact since it has test only at the moment
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )

lazy val sparkCobol = (project in file("spark-cobol"))
  .settings(
    name := "spark-cobol",
    printSparkVersion := {
      val log = streams.value.log
      log.info(s"Building with Spark ${sparkVersion(scalaVersion.value)}, Scala ${scalaVersion.value}")
      sparkVersion(scalaVersion.value)
    },
    Compile / compile := ((Compile / compile) dependsOn printSparkVersion).value,
    Compile / unmanagedSourceDirectories += {
      val sourceDir = (Compile / sourceDirectory).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n == 11 => sourceDir / "scala_2.11"
        case Some((2, n)) if n == 12 => sourceDir / "scala_2.12"
        case Some((2, n)) if n == 13 => sourceDir / "scala_2.13"
        case _ => throw new RuntimeException("Unsupported Scala version")
      }
    },
    libraryDependencies ++= SparkCobolDependencies(scalaVersion.value) :+ getScalaDependency(scalaVersion.value),
    dependencyOverrides ++= SparkCobolDependenciesOverride,
    Test / fork := true, // Spark tests fail randomly otherwise
    populateBuildInfoTemplate,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    assemblySettings
  ).dependsOn(cobolParser)
  .settings(
    jacocoReportSettings := commonJacocoReportSettings.withTitle("cobrix:spark-cobol Jacoco Report"),
    jacocoExcludes := commonJacocoExcludes
  )
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
  assembly / assemblyMergeStrategy := {
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
  assembly / assemblyOption:= (assembly / assemblyOption).value.copy(includeScala = false),
  assembly / assemblyShadeRules:= Seq(
    // Spark may rely on a different version of ANTLR runtime. Renaming the package helps avoid the binary incompatibility
    ShadeRule.rename("org.antlr.**" -> "za.co.absa.cobrix.cobol.parser.shaded.org.antlr.@1").inAll,
    // The SLF4j API and implementation are provided by Spark
    ShadeRule.zap("org.slf4j.**").inAll
  ),
  assembly / assemblyJarName := s"${name.value}_${scalaBinaryVersion.value}_${sparkVersionShort(scalaVersion.value)}-${version.value}-bundle.jar",
  assembly / logLevel := Level.Info,
  assembly / test := {}
)
