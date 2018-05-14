import warwick.Testing._
import warwick.Gulp

name := """start"""

version := "1.0-SNAPSHOT"

scalaVersion := Common.scalaVersion

// ULTRAVIOLENCE
scalacOptions ++= Seq("-language:implicitConversions", "-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

// Avoid some of the constant SBT "Updating"
updateOptions := updateOptions.value.withCachedResolution(true)

val gitRevision = SettingKey[String]("gitRevision")
gitRevision := git.gitHeadCommit.value.getOrElse("Unset")

lazy val root = (project in file("."))
  .enablePlugins(WarwickProject, PlayScala, PlayNettyServer, BuildInfoPlugin)
  .disablePlugins(PlayAkkaHttpServer)
  .configs(config("fun").extend(Test))
  .settings(
    Gulp.settings,
    // Package up assets before we build tar.gz
    packageZipTarball in Universal := (packageZipTarball in Universal).dependsOn(Gulp.gulpAssets).value,
    funSettings,
    buildInfoKeys := Seq[BuildInfoKey](name, version, gitRevision),
    buildInfoPackage := "info"
  )

// Versions of things for below
val enumeratumVersion = "1.5.12"
val akkaVersion = "2.5.3"
val playUtilsVersion = "1.11"

val appDeps = Seq(
  jdbc,
  cacheApi,
  ws,
  filters,
  evolutions,
  guice,
  jodaForms,
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "com.oracle" % "ojdbc7" % "12.1.0.2.0",
  "uk.ac.warwick.sso" %% "sso-client-play" % "2.40-SNAPSHOT",
  "uk.ac.warwick.play-utils" %% "accesslog" % playUtilsVersion,
  "uk.ac.warwick.play-utils" %% "anorm" % playUtilsVersion,
  "uk.ac.warwick.play-utils" %% "objectstore" % playUtilsVersion,
  "uk.ac.warwick.util" % "warwickutils-cache" % "20171206",
  "uk.ac.warwick.util" % "warwickutils-core" % "20171206",
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.kenshoo" %% "metrics-play" % "2.6.6_0.6.2",
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
  "org.apache.commons" % "commons-email" % "1.5",
  "com.notnoop.apns" % "apns" % "1.0.0.Beta6",
  "org.quartz-scheduler" % "quartz" % "2.2.1",
  "com.google.inject.extensions" % "guice-multibindings" % "4.0",
  "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "com.github.mumoshu" %% "play2-memcached-play26" % "0.9.2-warwick",
  "ch.qos.logback" % "logback-access" % "1.1.7",
//  "com.google.guava" % "guava" % "22.0",
  "com.google.apis" % "google-api-services-analyticsreporting" % "v4-rev10-1.22.0"
    exclude("com.google.guava","guava-jdk5"),
  "com.google.api-client" % "google-api-client" % "1.23.0",
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-play" % enumeratumVersion,
  "com.beachape" %% "enumeratum-play-json" % enumeratumVersion,
  "nl.martijndwars" % "web-push" % "2.0.0",
  "com.vladsch.flexmark" % "flexmark" % "0.32.18",
  "com.vladsch.flexmark" % "flexmark-ext-autolink" % "0.32.18",
  "com.googlecode.libphonenumber" % "libphonenumber" % "8.6.0",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.0.0"
)

val testDeps = Seq(
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.scalatest" %% "scalatest" % "3.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.19",
  "uk.ac.warwick.sso" %% "sso-client-play-testing" % "2.37",
  "org.eclipse.jetty" % "jetty-server" % "9.4.7.v20170914",
  "com.h2database" % "h2" % "1.4.196"
).map(_ % Test)

val funcTestDeps = Seq(
  // Note - from 2.53 selenium-htmlunit is not bundled so will need to
  // play with dependencies if you need to upgrade.
  "org.seleniumhq.selenium" % "selenium-java" % "3.4.0"
).map(_ % "fun")

javaOptions in Test += "-Dlogger.resource=test-logging.xml"

libraryDependencies ++= (appDeps ++ testDeps ++ funcTestDeps).map(_.excludeAll(
  ExclusionRule(organization = "commons-logging"),
  // ehcache renamed ehcache-core, don't load in the old version
  ExclusionRule(organization = "net.sf.ehcache", name = "ehcache")
))

// https://bugs.elab.warwick.ac.uk/browse/SSO-1653
dependencyOverrides += "xml-apis" % "xml-apis" % "1.4.01"

// Because jclouds is terrible
dependencyOverrides += "com.google.guava" % "guava" % "20.0"

// Because jclouds is terrible
dependencyOverrides += "com.google.code.gson" % "gson" % "2.4"

// Fix a dependency warning
dependencyOverrides += "org.json" % "json" % "20171018"

// Make gulp output available as Play assets.
unmanagedResourceDirectories in Assets += baseDirectory.value / "target" / "gulp"

resolvers += Resolver.mavenLocal

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "elastic-lucene-snapshots" at "http://s3.amazonaws.com/download.elasticsearch.org/lucenesnapshots/a128fcb"

resolvers += "jclouds-snapshots" at "https://repository.apache.org/content/repositories/snapshots"

// Run Gulp when Play runs
//playRunHooks <+= baseDirectory.map(base => Gulp(base))

// code coverage settings (run jacoco:cover)
jacoco.settings

parallelExecution in jacoco.Config := false

TwirlKeys.templateImports ++= Seq("views.utils._")
