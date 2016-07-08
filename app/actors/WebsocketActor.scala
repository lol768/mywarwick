package actors

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import models.ActivityResponse
import play.api.libs.json._
import system.AppMetrics.RequestTracker
import warwick.sso.LoginContext

object WebsocketActor {

  def props(loginContext: LoginContext, tracker: RequestTracker)(out: ActorRef) = Props(classOf[WebsocketActor], out, loginContext, tracker)

  /**
    * For realtime notification/activity stream updates. This message is published to PubSub
    * and received by all open websockets for the relevant users.
    */
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
class WebsocketActor(out: ActorRef, loginContext: LoginContext, tracker: RequestTracker) extends Actor with ActorLogging {

  import WebsocketActor._

  loginContext.user.foreach { user =>
    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe(user.usercode.string, self)
  }

  val signedIn = loginContext.user.exists(_.isFound)
  val who = loginContext.user.flatMap(_.name.full).getOrElse("nobody")

  import ActivityResponse.writes

  override def postStop(): Unit = {
    tracker.stop()
  }

  override def receive = {
    //
    case Notification(activity) => out ! Json.obj(
      "type" -> "activity",
      "activity" -> activity
    )
    case SubscribeAck(Subscribe(topic, group, ref)) =>
      log.debug(s"Websocket subscribed to PubSub messages on the topic of '${topic}'")
    case nonsense => log.error(s"Ignoring unrecognised message: ${nonsense}")
  }

}


