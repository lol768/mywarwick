package actors

import javax.inject.Inject

import akka.actor.{ActorLogging, Actor}
import play.api.Configuration

// Just testing DI
class SystemRoot @Inject() (config: Configuration) extends Actor with ActorLogging {

  log.info("SystemRoot created")

  // echo
  def receive = {
    case any => sender ! any
  }

}
