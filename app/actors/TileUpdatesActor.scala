package actors

import actors.TileActor.TileUpdate
import akka.actor._
import play.api.Logger
import play.api.libs.json._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.Random

object TileWebsocketActor  {
  def props(out: ActorRef) = Props(new TileWebsocketActor(out))
}

/**
 * Websocket-facing actor, wired in to the controller. Receives messages from
 * the client using
 *
 * @param out this output will be attached to the websocket and will send
 *            messages back to the client.
 */
class TileWebsocketActor(out : ActorRef) extends Actor {
  import TileWebsocketActor._
  import context.dispatcher

  val fakeTiles = for (i <- 1 to 50) yield {
    context.actorOf(Props(classOf[TileActor], i.toString))
  }


  override def receive = {
    case TileUpdate(data) => out ! data
    case o : JsValue =>
      out ! Json.obj(
        "message" -> "I got your message, but I haven't done anything about it."
      )
  }

}

object TileActor {
  def props(tileId: String) = Props(new TileActor(tileId))

  case class TileUpdate(val data: JsValue)
}

class TileActor(val tileId: String) extends Actor {
  import TileActor._

  val parent = context.actorSelection("..")

  context.system.scheduler.schedule(0 millis, (1400 + Random.nextInt(400)) millis) {
    parent ! TileUpdate(Json.obj(
      "tileId" -> tileId,
      "value" -> Random.nextInt(1000)
    ))
  }

  def receive = {
    case x => Logger.info("Hello: " + x)
  }
}

