import Gulp._
import play.sbt.PlayImport.PlayKeys._

name := """start"""

version := "1.0-SNAPSHOT"

scalaVersion := Common.scalaVersion

val gulpAssetsTask = TaskKey[Unit]("gulp-assets")

lazy val main = (project in file(".")).enablePlugins(PlayScala).dependsOn(admin).aggregate(admin).settings(
  gulpAssetsTask := Gulp(baseDirectory.value).buildAssets(),

  dist <<= (dist) dependsOn (gulpAssetsTask),
  assembly <<= assembly.dependsOn(gulpAssetsTask)
)

lazy val admin = (project in file("modules/admin")).enablePlugins(PlayScala)

// Set up a phat jar
test in assembly := {}
mainClass in assembly := Some("play.core.server.ProdServerStart")
fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
assemblyMergeStrategy in assembly := {
  case "pom.xml" | "pom.properties" => MergeStrategy.discard
  case other => (assemblyMergeStrategy in assembly).value(other) // use default
}

val appDeps = Seq(
  jdbc,
  cache,
  ws,
  filters,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.4.0")

val testDeps = Seq(
  specs2,
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12"
).map(_ % Test)

libraryDependencies ++= (appDeps ++ testDeps).map(_.excludeAll(
  ExclusionRule(organization = "commons-logging")
))

// Make gulp output available as Play assets.
unmanagedResourceDirectories in Assets <+= baseDirectory { _ / "target" / "gulp" }

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Run Gulp when Play runs
//playRunHooks <+= baseDirectory.map(base => Gulp(base))
