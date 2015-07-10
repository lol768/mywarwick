name := """start"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

//val webjars = Seq(
//  "org.webjars" %% "webjars-play" % "2.4.0-1", // A helper
//  "org.webjars.bower" % "requirejs" % "2.1.18",
//  "org.webjars" % "bootstrap" % "3.3.5",
//  "org.webjars.bower" % "react" % "0.13.3",
//  "org.webjars" % "jsx-requirejs-plugin" % "0.6.0"
//)

//pipelineStages := Seq(rjs, digest)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator