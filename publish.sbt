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
