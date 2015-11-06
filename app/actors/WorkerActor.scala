package actors

import javax.inject.Inject

import akka.actor._
import play.api.Configuration

class WorkerActor @Inject()(config: Configuration) extends Actor with ActorLogging {

  // echo
  def receive = ???

}