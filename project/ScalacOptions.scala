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

object ScalacOptions {
  val scalacOptionsForAllVersions = Seq(
    "-encoding", "UTF-8",         // source files are in UTF-8
    "-deprecation",               // warn about use of deprecated APIs
    "-unchecked",                 // warn about unchecked type parameters
    "-feature",                   // warn about misused language features
    "-explaintypes"               // explain type errors in more detail
  )

  val compilerWarningOptions = Seq(
    "-opt:l:inline",             // enable inline optimizations ...
    "-opt-inline-from:<source>", // ... from source files
    "-opt-warnings",             // enable optimizer warnings
    "-Ywarn-extra-implicit",     // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen",      // Warn when numerics are widened.
    "-Ywarn-unused:implicits",   // Warn if an implicit parameter is unused.
    "-Ywarn-unused:locals",      // Warn if a local definition is unused.
    "-Ywarn-unused:params",      // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",     // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",    // Warn if a private member is unused.
    "-Ywarn-value-discard"       // Warn when non-Unit expression results are unused.
  )

  lazy val scalacOptions211 = scalacOptionsForAllVersions ++
    Seq(
      "-Xsource:2.11",           // Treat compiler input as Scala source for scala-2.11
      "-target:jvm-1.7"          // Target JVM 1.7
    )

  lazy val scalacOptions212 = scalacOptionsForAllVersions ++ compilerWarningOptions ++
    Seq(
      "-Xsource:2.12",           // Treat compiler input as Scala source for scala-2.12
      "-target:jvm-1.8"          // Target JVM 1.8
    )

  lazy val scalacOptions213 = scalacOptionsForAllVersions ++ compilerWarningOptions ++
    Seq(
      "-Xsource:2.13",           // Treat compiler input as Scala source for scala-2.13
      "-target:jvm-1.8"          // Target JVM 1.8
    )

  def scalacOptionsFor(scalaVersion: String): Seq[String] = {
    val scalacOptions = CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, minor)) if minor >= 13 =>
        scalacOptions213
      case Some((2, minor)) if minor == 12 =>
        scalacOptions212
      case _ =>
        scalacOptions211
    }
    println(s"Scala $scalaVersion compiler options: ${scalacOptions.mkString(" ")}")
    scalacOptions
  }
}
