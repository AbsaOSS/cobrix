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

ThisBuild / organizationName := "ABSA Group Limited"
ThisBuild / organizationHomepage := Some(url("https://www.absa.africa"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    browseUrl = url("http://github.com/AbsaOSS/cobrix/tree/master"),
    connection = "scm:git:git://github.com/AbsaOSS/cobrix.git",
    devConnection = "scm:git:ssh://github.com/AbsaOSS/cobrix.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "felipemmelo",
    name  = "Felipe Melo",
    email = "felipe.melo@absa.africa",
    url   = url("https://github.com/felipemmelo")
  ),
  Developer(
    id    = "yruslan",
    name  = "Ruslan Iushchenko",
    email = "ruslan.iushchenko@absa.africa",
    url   = url("https://github.com/yruslan")
  )
)

ThisBuild / homepage := Some(url("https://github.com/AbsaOSS/cobrix"))
ThisBuild / description := "COBOL Reading and Import Extensions for Apache Spark"
ThisBuild / startYear := Some(2018)
ThisBuild / licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) {
    Some("snapshots" at s"${nexus}content/repositories/snapshots")
  } else {
    Some("releases" at s"${nexus}service/local/staging/deploy/maven2")
  }
}
ThisBuild / publishMavenStyle := true
