package actors

import javax.inject.Inject

import akka.pattern._
import akka.actor.{Props, Actor}
import services.PrinterService

import scala.util.{Failure, Success}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object PrinterActor {
  def props(service: PrinterService) = Props(classOf[PrinterActor], service)
}

/**
 * An actor that asks PrinterService for some info and replies.
 */
class PrinterActor @Inject() (printerService: PrinterService) extends Actor {
  def receive = {
    case user: String => printerService.get(user).pipeTo(sender)
  }
}

