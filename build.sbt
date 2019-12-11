/*
 * Copyright 2018-2019 ABSA Group Limited
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
    name := "cobrix"
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