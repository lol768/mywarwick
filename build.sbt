import warwick.Testing._
import warwick.Gulp

name := """start"""

version := "1.0-SNAPSHOT"

scalaVersion := Common.scalaVersion

// ULTRAVIOLENCE
scalacOptions ++= Seq("-language:implicitConversions", "-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

// Avoid some of the constant SBT "Updating"
updateOptions := updateOptions.value.withCachedResolution(true)

lazy val root = (project in file(".")).enablePlugins(WarwickProject, PlayScala)
  .configs(FunTest)
  .settings(
    Gulp.settings,
    // Package up assets before we build tar.gz
    packageZipTarball in Universal := (packageZipTarball in Universal).dependsOn(Gulp.gulpAssets).value,
    funSettings
  )

// Versions of things for below
val enumeratumVersion = "1.4.4"

val appDeps = Seq(
  jdbc,
  cache,
  ws,
  filters,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.5.0",
  "com.oracle" % "ojdbc7" % "12.1.0.2.0",
  "uk.ac.warwick.sso" %% "sso-client-play" % "2.22",
  "uk.ac.warwick.play-utils" %% "accesslog" % "1.7",
  "uk.ac.warwick.play-utils" %% "anorm" % "1.7",
  "uk.ac.warwick.play-utils" %% "objectstore" % "1.7",
  "uk.ac.warwick.util" % "warwickutils-cache" % "20160429",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.16",
  "com.typesafe.akka" %% "akka-cluster-tools" % "2.4.16",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.16",
  "com.kenshoo" %% "metrics-play" % "2.5.0_0.5.0-play-2.5-fix",
  "com.typesafe.play" %% "play-mailer" % "5.0.0-M1",
  "com.notnoop.apns" % "apns" % "1.0.0.Beta6",
  "org.quartz-scheduler" % "quartz" % "2.2.1",
  "com.google.inject.extensions" % "guice-multibindings" % "4.0",
  "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0",
  "ch.qos.logback" % "logback-access" % "1.1.7",
  "com.google.apis" % "google-api-services-analyticsreporting" % "v4-rev10-1.22.0",
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-play" % enumeratumVersion,
  "com.beachape" %% "enumeratum-play-json" % enumeratumVersion,
  "nl.martijndwars" % "web-push" % "2.0.0"
)

val testDeps = Seq(
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.scalatest" %% "scalatest" % "2.2.5",
  "org.scalatestplus" %% "play" % "1.4.0-M4",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.0",
  "uk.ac.warwick.sso" %% "sso-client-play-testing" % "2.18",
  "org.eclipse.jetty" % "jetty-server" % "9.3.6.v20151106",
  // Note - from 2.53 selenium-htmlunit is not bundled so will need to
  // play with dependencies if you need to upgrade.
  "org.seleniumhq.selenium" % "selenium-java" % "2.52.0"
).map(_ % Test)

javaOptions in Test += "-Dlogger.resource=test-logging.xml"

libraryDependencies ++= (appDeps ++ testDeps).map(_.excludeAll(
  ExclusionRule(organization = "commons-logging"),
  // ehcache renamed ehcache-core, don't load in the old version
  ExclusionRule(organization = "net.sf.ehcache", name = "ehcache")
))

// https://bugs.elab.warwick.ac.uk/browse/SSO-1653
dependencyOverrides += "xml-apis" % "xml-apis" % "1.4.01"

// NEWSTART-407
dependencyOverrides += "ch.qos.logback" % "logback-classic" % "1.1.7"

// Make gulp output available as Play assets.
unmanagedResourceDirectories in Assets += baseDirectory.value / "target" / "gulp"

resolvers += ("Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.m2/repository")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Run Gulp when Play runs
//playRunHooks <+= baseDirectory.map(base => Gulp(base))

// code coverage settings (run jacoco:cover)
jacoco.settings

parallelExecution in jacoco.Config := false

guiceVersionFixes

