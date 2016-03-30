import play.sbt.PlayRunHook
import sbt.Keys._
import sbt._

/**
 * Assists with running Gulp from an SBT build.
 *
 * Requires an installation of `node`.
 */
object Gulp {

  lazy val gulpAssets = taskKey[Unit]("Builds static assets using Gulp.")
  lazy val gulp = inputKey[Unit]("Runs Gulp, passing any arguments.")

  lazy val settings = Seq(
    gulpAssets := Gulp(baseDirectory.value).buildAssets(),
    gulp := {
      val yeezy = Def.spaceDelimited("<arg>").parsed
      Gulp(baseDirectory.value).gulp(yeezy)
    }
  )

  class GulpProcess(base: File) extends PlayRunHook {
    def buildAssets(): Unit = {
      runSuccessfully("npm install")
      gulp(Seq("assets"))
    }

    def gulp(args: Seq[String]): Unit = {
      runSuccessfully("node_modules/.bin/gulp " + args.mkString(" "))
    }

    private def runSuccessfully(command: String, expectedExitCode: Int = 0): Unit = {
      val exitCode = Process(command).!

      if (exitCode != expectedExitCode) {
        throw new Exception(s"""Command "$command" failed (exit code was $exitCode, expected $expectedExitCode)""")
      }
    }

    override def beforeStarted() = buildAssets
  }

  def apply(base: File): GulpProcess = new GulpProcess(base)

}