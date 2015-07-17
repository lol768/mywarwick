package tasks

import play.api.{Environment, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * Standalone process that loads config from worker.conf
 * and
 */
object Worker extends App {

  val config = Configuration.load(Environment.simple(), Map(
    "config.resource" -> "worker.conf"
  ))

  val app = new GuiceApplicationBuilder(
    configuration = config
  ).build()

}
