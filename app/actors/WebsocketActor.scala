package actors

import akka.actor._
import org.joda.time.DateTime
import play.api.data.validation.ValidationError
import play.api.libs.json._
import scala.concurrent.duration._

object WebsocketActor {
  def props(out: ActorRef) = Props(classOf[WebsocketActor], out)

  /**
   * This is the format of the JSON that client web pages will send to
   * the server
   *
   * TODO will all messages really have a tileId?
   *
   * @param messageId Sequential ID provided by client to use in replies.
   *                  This is short-lived and only unique within a web page load.
   * @param tileId Id of the tile that is requesting information.
   * @param data Freeform JSON payload.
   */
  case class ClientData(messageId: Int, tileId: String, data: JsValue)

  implicit val clientDataFormat = Json.format[ClientData]

  /**
   * Wrapper for ClientData when passing it on to other actors.
   * @param sender where to send replies.
   * @param data the ClientData.
   */
  case class ClientDataWrapper(sender: ActorRef, data: ClientData)

  // An update to a single tile
  case class TileUpdate(data: JsValue)

}

/**
 * Websocket-facing actor, wired in to the controller. It receives
 * any messages sent from the client in the form of a JsValue. Other
 * actors can also send any kind of message to it.
 *
 * Currently this contains a lot of stuff but only because it's generating
 * a bunch of fake data as we have no backend yet. When it's done, it
 * ought to be pretty slim as it will mainly just subscribe to some actor
 * within the larger system, passing data to the websocket.
 *
 * @param out this output will be attached to the websocket and will send
 *            messages back to the client.
 */
class WebsocketActor(out: ActorRef) extends Actor with ActorLogging {

  import WebsocketActor._

  def sendNotification(id: String, text: String, source: String, date: String): Unit = {
    out ! JsObject(Seq(
      "id" -> JsString(id),
      "type" -> JsString("notification"),
      "text" -> JsString(text),
      "source" -> JsString(source),
      "date" -> JsString(date)
    ))
  }

  import context.dispatcher

  context.system.scheduler.schedule(5 seconds, 9 seconds) {
    sendNotification(
      DateTime.now().toString,
      "Your submission for CH155 Huge Essay is due tomorrow",
      "Tabula",
      DateTime.now().toString
    )
  }

  // FIXME UserMessageHandler and the way we create it here is non-production.
  // it likely shouldn't own UserMessageHandler as a child (which is what context.actorOf
  // does here).
  val handler = context.actorOf(Props[UserMessageHandler])

  override def receive = {
    // these will be the TileUpdates we send to ourself.
    case TileUpdate(data) => {
      log.info("About to pipe out some data")
      out ! data
    }
    case js: JsValue => handleClientMessage(js)
    case nonsense => log.error(s"Ignoring unrecognised message: ${nonsense}")
  }

  def handleClientMessage(js: JsValue): Unit = {
    log.debug("Got a JsValue message from client")
    js.validate[ClientData].fold(
      errors => out ! toErrorResponse(js, errors),
      clientData => handleClientData(clientData)
    )
  }

  def handleClientData(clientData: ClientData) = {
    log.debug("Successfully parsed ClientData: {}", clientData)
    handler ! ClientDataWrapper(out, clientData)
  }

  def toErrorResponse(js: JsValue, errors: Seq[(JsPath, Seq[ValidationError])]) = {
    val messageId = (js \ "messageId").validate[Int]
    val errorsSeq = Json.arr(Json.obj(
      "code" -> "400",
      "title" -> "JSON parse error",
      "detail" -> errors.toString()
    ))

    val msg = messageId.map(id =>
      Json.obj(
        "errorFor" -> id,
        "errors" -> errorsSeq
      )
    ).getOrElse(
      Json.obj(
        "errors" -> errorsSeq
      )
    )
  }

}


