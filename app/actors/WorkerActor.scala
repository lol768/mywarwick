package actors

import java.io.IOException
import javax.inject.Inject

import akka.actor.SupervisorStrategy._

import akka.actor._
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class WorkerActor @Inject() (config: Configuration) extends Actor with ActorLogging {

  log.info("Worker actor created")

  // set behaviour for when a child throws a wobbly.
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case io: IOException => Resume
    case _ => Restart
  }

  val mySon = context.actorOf(Props[StupidActor], "alan")

  // echo
  def receive = {
    case any => sender ! any
  }

}

class StupidActor extends Actor with ActorLogging {

  log.info("Stupid actor being created.")

  self ! Kill

  def receive = {
    case _ => // drop
  }

}