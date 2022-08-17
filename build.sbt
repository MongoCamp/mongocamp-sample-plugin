import scala.io.Source
import scala.tools.nsc.io.File

organization := "com.quadstingray"

scalaVersion := "2.13.8"

ThisBuild / version := PackageTools.packageJson().sbtStyleVersion

name := PackageTools.packageJson().name

libraryDependencies += "dev.mongocamp" %% "mongocamp-server" % "1.2.1"

publishTo := Some("GitHub Package Registry".at("https://maven.pkg.github.com/mongocamp/mongocamp-sample-plugin/"))

credentials += Credentials("GitHub Package Registry", "maven.pkg.github.com", System.getenv("GITHUB_USER"), System.getenv("GITHUB_TOKEN"))

mainClass := Some("dev.mongocamp.server.Server")
