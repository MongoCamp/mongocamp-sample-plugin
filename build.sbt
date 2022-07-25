name := "mongocamp-sample-plugin"

organization := "com.quadstingray"

scalaVersion := "2.13.8"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "dev.mongocamp" %% "mongocamp-server" % "1.2.1" % Provided

publishTo := Some("GitHub Package Registry".at("https://maven.pkg.github.com/mongocamp/mongocamp-sample-plugin/"))

credentials += Credentials("GitHub Package Registry", "maven.pkg.github.com", System.getenv("GITHUB_USER"), System.getenv("GITHUB_TOKEN"))

