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
      Process("npm install").!
      Process("node_modules/.bin/gulp assets").!
    }

    override def beforeStarted() = buildAssets
  }

  def apply(base: File): GulpProcess = new GulpProcess(base)

}