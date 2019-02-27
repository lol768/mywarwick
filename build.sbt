import warwick.Testing._
import warwick.Gulp

organization := "uk.ac.warwick"
name := """my-warwick"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.7"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions ++= Seq(
  "-encoding", "UTF-8", // yes, this is 2 args
  "-target:jvm-1.8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Xfatal-warnings"
)
scalacOptions in Test ++= Seq("-Yrangepos")
scalacOptions in (Compile, doc) ++= Seq("-no-link-warnings")

// Avoid some of the constant SBT "Updating"
updateOptions := updateOptions.value.withCachedResolution(true)

val gitRevision = SettingKey[String]("gitRevision")
gitRevision := git.gitHeadCommit.value.getOrElse("Unset")

lazy val root = (project in file("."))
  .enablePlugins(WarwickProject, PlayScala, BuildInfoPlugin)
  .configs({
    val Fun = config("fun").extend(Test)
    Fun
  })
  .settings(
    Gulp.settings,
    // Package up assets before we build tar.gz
    packageZipTarball in Universal := (packageZipTarball in Universal).dependsOn(Gulp.gulpAssets).value,
    funSettings,
    buildInfoKeys := Seq[BuildInfoKey](name, version, gitRevision),
    buildInfoPackage := "info"
  )

// Versions of things for below
val enumeratumVersion = "1.5.13"
val akkaVersion = "2.5.21"
val playUtilsVersion = "1.29"
val ssoClientVersion = "2.63"
val warwickUtilsVersion = "20190221"

val appDeps = Seq(
  jdbc,
  cacheApi,
  ws,
  filters,
  evolutions,
  guice,
  jodaForms,
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  "com.oracle" % "ojdbc8" % "12.2.0.1.0",
  "uk.ac.warwick.sso" %% "sso-client-play" % ssoClientVersion,
  "uk.ac.warwick.play-utils" %% "accesslog" % playUtilsVersion,
  "uk.ac.warwick.play-utils" %% "anorm" % playUtilsVersion,
  "uk.ac.warwick.play-utils" %% "objectstore" % playUtilsVersion,
  "uk.ac.warwick.util" % "warwickutils-cache" % warwickUtilsVersion,
  "uk.ac.warwick.util" % "warwickutils-core" % warwickUtilsVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "com.kenshoo" %% "metrics-play" % "2.7.0_0.8.0",
  "com.typesafe.play" %% "play-mailer" % "7.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "7.0.0",
  "com.notnoop.apns" % "apns" % "1.0.0.Beta6",
  "org.quartz-scheduler" % "quartz" % "2.3.0" exclude("com.zaxxer", "HikariCP-java6"),
  "com.google.inject.extensions" % "guice-multibindings" % "4.2.2",
  "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "com.github.mumoshu" %% "play2-memcached-play27" % "0.10.0-RC3",
  "com.google.apis" % "google-api-services-analyticsreporting" % "v4-rev128-1.25.0"
    exclude("com.google.guava","guava-jdk5"),
  "com.google.api-client" % "google-api-client" % "1.28.0",
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-play" % enumeratumVersion,
  "com.beachape" %% "enumeratum-play-json" % enumeratumVersion,
  "nl.martijndwars" % "web-push" % "5.0.1",
  "com.vladsch.flexmark" % "flexmark" % "0.40.18",
  "com.vladsch.flexmark" % "flexmark-ext-autolink" % "0.40.18",
  "com.googlecode.libphonenumber" % "libphonenumber" % "8.6.0",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.6.1",
  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.11.2",
  "com.rometools" % "rome" % "1.12.0"
)

val testDeps = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1",
  "org.mockito" % "mockito-core" % "2.24.5",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "uk.ac.warwick.sso" %% "sso-client-play-testing" % ssoClientVersion,
  "org.eclipse.jetty" % "jetty-server" % "9.4.15.v20190215",
  "com.h2database" % "h2" % "1.4.198"
).map(_ % Test)

val funcTestDeps = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1"
).map(_ % "fun")

javaOptions in Test += "-Dlogger.resource=test-logging.xml"

libraryDependencies ++= (appDeps ++ testDeps ++ funcTestDeps).map(_.excludeAll(
  ExclusionRule(organization = "commons-logging"),
  // No EhCache please we're British
  ExclusionRule(organization = "net.sf.ehcache"),
  ExclusionRule(organization = "org.ehcache"),
  ExclusionRule(organization = "ehcache")
))

// https://bugs.elab.warwick.ac.uk/browse/SSO-1653
dependencyOverrides += "xml-apis" % "xml-apis" % "1.4.01"

// JClouds requires v2.5 https://issues.apache.org/jira/browse/JCLOUDS-1166
dependencyOverrides += "com.google.code.gson" % "gson" % "2.5"

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

TwirlKeys.templateImports ++= Seq("views.utils._")
