lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.10"

ThisBuild / organization := "za.co.absa.cobrix"

ThisBuild / scalaVersion := scala211
ThisBuild / crossScalaVersions := Seq(scala211, scala212)

ThisBuild / Test / javaOptions += "-Xmx2G"

import Dependencies._
import BuildInfoTemplateSettings._

lazy val cobrix = (project in file("."))
  .settings(
    name := "cborix"
  )
  .aggregate(cobolParser, sparkCobol)

lazy val cobolParser = (project in file("cobol-parser"))
  .settings(
    name := "cobol-parser",
    libraryDependencies ++= CobolParserDependencies
  )

lazy val sparkCobol = (project in file("spark-cobol"))
  .settings(
    name := "spark-cobol",
    libraryDependencies ++= SparkCobolDependencies,
    dependencyOverrides ++= SparkCobolDependenciesOverride,
    Test / fork := true, // Spark tests fail randomly otherwise
    populateBuildInfoTemplate
  )
  .dependsOn(cobolParser)

// scoverage settings
ThisBuild / coverageExcludedPackages := ".*examples.*;.*replication.*"
ThisBuild / coverageExcludedFiles := ".*Example.*;Test.*"

// release settings
releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseCrossBuild := true
addCommandAlias("releaseMajor", ";set releaseVersionBump := sbtrelease.Version.Bump.Major; release with-defaults")
addCommandAlias("releaseMinor", ";set releaseVersionBump := sbtrelease.Version.Bump.Minor; release with-defaults")
addCommandAlias("releasePatch", ";set releaseVersionBump := sbtrelease.Version.Bump.Bugfix; release with-defaults")