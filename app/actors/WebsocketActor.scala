package actors

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{SubscribeAck, Subscribe}
import models.{ActivityResponse, Activity, ActivityType}
import org.joda.time.DateTime
import play.api.data.validation.ValidationError
import play.api.libs.json._
import warwick.sso.LoginContext

import scala.concurrent.duration._

object WebsocketActor {
  implicit val clientDataFormat = Json.format[ClientData]

  def props(loginContext: LoginContext)(out: ActorRef) = Props(classOf[WebsocketActor], out, loginContext)

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

  /**
    * Wrapper for ClientData when passing it on to other actors.
    * @param sender where to send replies.
    * @param data the ClientData.
    */
  case class ClientDataWrapper(sender: ActorRef, data: ClientData)

  case class Notification(activity: ActivityResponse)

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
class WebsocketActor(out: ActorRef, loginContext: LoginContext) extends Actor with ActorLogging {

  import WebsocketActor._
  import context.dispatcher

  loginContext.user.foreach { user =>
    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe(user.usercode.string, self)
  }

  // FIXME UserMessageHandler and the way we create it here is non-production.
  // it likely shouldn't own UserMessageHandler as a child (which is what context.actorOf
  // does here).
  val handler = context.actorOf(UserMessageHandler.props(loginContext))

  val signedIn = loginContext.user.exists(_.isFound)
  val who = loginContext.user.flatMap(_.name.full).getOrElse("nobody")

  import ActivityResponse.writes

  override def receive = {
    case Notification(activity) => out ! Json.obj(
      "type" -> "activity",
      "activity" -> activity
    )
    case js: JsValue => handleClientMessage(js)
    case SubscribeAck(Subscribe(topic, group, ref)) =>
      log.debug(s"Websocket subscribed to PubSub messages on the topic of '${topic}'")
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


