import Gulp._
import play.sbt.PlayImport.PlayKeys._

name := """start"""

version := "1.0-SNAPSHOT"

val gulpAssetsTask = TaskKey[Unit]("gulp-assets")

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(
  gulpAssetsTask := Gulp(baseDirectory.value).buildAssets(),

  dist <<= (dist) dependsOn (gulpAssetsTask),
  assembly <<= (assembly) dependsOn (gulpAssetsTask)
)

scalaVersion := "2.11.6"

mainClass in assembly := Some("play.core.server.ProdServerStart")
test in assembly := {}
fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
assemblyMergeStrategy in assembly := {
  case "pom.xml" | "pom.properties" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  specs2 % Test
).map(_.excludeAll(
  ExclusionRule(organization = "commons-logging")
))



unmanagedResourceDirectories in Assets <+= baseDirectory { _ / "target" / "gulp" }

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Run Gulp when Play runs
playRunHooks <+= baseDirectory.map(base => Gulp(base))

// Run gulp when building a distribution
