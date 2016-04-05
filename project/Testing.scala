import sbt.Keys._
import sbt._
import com.typesafe.sbt.web.Import._

/**
  * All the annoying bits of SBT config to allow us to
  * do functional tests and stuff.
  */
object Testing {

  /**
    * FUNCTIONAL TESTING
    * Functional tests are expected under functional-test
    */

  /**
    * Define this on your project using .configs(FunTest)
    */
  lazy val FunTest = config("fun").extend(Test)

  /**
    * Drop this into your project settings list.
    */
  lazy val funSettings: Seq[Setting[_]] =
    inConfig(FunTest)(Defaults.testSettings ++ Seq(
      // Only bothering to update the scala source, sorry if you're
      // trying to add .java sources and getting confused.
      scalaSource := baseDirectory.value / "functional-test",

      unmanagedResourceDirectories := Seq(baseDirectory.value / "functional-test" / "resources"),

      // expose the web-assets jar that has js and stuff in it,
      // including Gulp stuff if that has been added to
      // `(unmanagedResourceDirectories in Assets)`.
      managedClasspath += (packageBin in Assets).value,

      // Allow access to resources under test, for things like
      // database migrations.
      unmanagedResourceDirectories ++= (unmanagedResourceDirectories in Test).value
    ))

}