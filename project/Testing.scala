import sbt.Keys._
import sbt._

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

      // Allow access to resources under test, for things like
      // database migrations.
      unmanagedResourceDirectories ++= (unmanagedResourceDirectories in Test).value
    ))

}