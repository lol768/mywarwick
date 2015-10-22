

name := """start"""

version := "1.0-SNAPSHOT"

scalaVersion := Common.scalaVersion

val gulpAssetsTask = TaskKey[Unit]("gulp-assets")

lazy val main = (project in file(".")).enablePlugins(PlayScala).dependsOn(admin).aggregate(admin).settings(
  gulpAssetsTask := Gulp(baseDirectory.value).buildAssets(),

  // Package up assets before we build tar.gz
  packageZipTarball in Universal <<= (packageZipTarball in Universal).dependsOn(gulpAssetsTask)
)


lazy val admin = (project in file("modules/admin")).enablePlugins(PlayScala)

val appDeps = Seq(
  jdbc,
  cache,
  ws,
  filters,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "com.oracle" % "ojdbc6" % "11.2.0.3.0",
  "uk.ac.warwick.sso" %% "sso-client-play" % "2.7-SNAPSHOT")

val testDeps = Seq(
  specs2,
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12"
).map(_ % Test)

libraryDependencies ++= (appDeps ++ testDeps).map(_.excludeAll(
  ExclusionRule(organization = "commons-logging")
))

// https://bugs.elab.warwick.ac.uk/browse/SSO-1653
dependencyOverrides += "xml-apis" % "xml-apis" % "1.4.01"

// Make gulp output available as Play assets.
unmanagedResourceDirectories in Assets <+= baseDirectory { _ / "target" / "gulp" }

// Configure the tar.gz generation how we like it
packagingSettings

resolvers += WarwickNexus
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Run Gulp when Play runs
//playRunHooks <+= baseDirectory.map(base => Gulp(base))
