import scala.io.Source
import scala.tools.nsc.io.File
import org.json4s.native.Serialization.read
import org.json4s.DefaultFormats

object PackageTools {
  implicit val formats = DefaultFormats

  private lazy val packageJsonInternal: PackageJson = {
    val packageJsonFile = File("package.json")
    val source          = Source.fromFile(packageJsonFile.toURI)
    read[PackageJson](source.mkString)
  }

  def packageJson(): PackageJson = packageJsonInternal
}

case class PackageJson(name: String, version: String) {
  def sbtStyleVersion: String = version.toLowerCase.replace(".snapshot", "-SNAPSHOT")
}
