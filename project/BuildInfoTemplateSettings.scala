import sbt.Keys._
import sbt._

object BuildInfoTemplateSettings {

  import java.time.LocalDateTime
  import java.time.format.DateTimeFormatter

  lazy val populateBuildInfoTemplate: Seq[Def.Setting[_]] = Seq(
    Compile / unmanagedResources / excludeFilter := excludeTemplateResource.value,
    Compile / resourceGenerators += populateResourceTemplate.taskValue
  )

  private val excludeTemplateResource = Def.setting {
    val propsTemplate = ((Compile / resourceDirectory).value / "cobrix_build.properties").getCanonicalPath
    new SimpleFileFilter(_.getCanonicalPath == propsTemplate)
  }

  private val populateResourceTemplate = Def.task {
    val template = IO.read((Compile / resourceDirectory).value / "cobrix_build.properties")
    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
    val filledTemplate = template
      .replace("${project.version}", version.value)
      .replace("${timestamp}", now)

    val out = (Compile / resourceManaged).value / "cobrix_build.properties"
    IO.write(out, filledTemplate)
    Seq(out)
  }

}
