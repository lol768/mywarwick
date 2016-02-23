import play.sbt.PlayRunHook
import com.typesafe.sbt.jse.SbtJsTask

import sbt._

import scala.sys.process.ProcessLogger

/**
 * Assists with running Gulp from an SBT build.
 *
 * Requires an installation of `node`.
 */
object Gulp {

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