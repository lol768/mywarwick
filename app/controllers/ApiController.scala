package controllers

import javax.inject.{Named, Inject, Singleton}

import actors.PrinterActor
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.pattern._
import akka.util.Timeout
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import services.PrinterService

import scala.concurrent.duration._

/**
 * This is me testing the Guice injection more than anything useful.
 *
 * Would be unlikely to interact with the actor system directly
 * from a controller.
 */
@Singleton
class ApiController @Inject() (
    actorSystem: ActorSystem,
    printerService: PrinterService,
    @Named("system-root-actor") systemRoot: ActorRef
  ) extends Controller {

  // Asking an actor for a reply will use this timeout
  implicit val timeout = Timeout(5 seconds)

  // Create an actor - passing in dependencies
  val printer = actorSystem.actorOf(Props(classOf[PrinterActor], printerService))

  // Use the `?`/ask pattern to send some messages to actors
  // and await a response, then display it
  def test = Action.async {
    for {
      result <- (printer ? "ron").mapTo[PrinterService.Response]
      echo   <- (systemRoot ? "ECHO!").mapTo[String]
    } yield {
      Ok(Json.obj(
        "actor"    -> true,
        "username" -> result.username,
        "balance"  -> result.balance,
        "echo"     -> echo
      ))
    }
  }

}
