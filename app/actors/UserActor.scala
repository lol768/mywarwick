package actors

import actors.UserActor.{WebSocketDisconnect, WebSocketConnect, Notification}
import akka.actor.{ActorRef, Actor, ActorLogging}
import models.Activity

object UserActor {

  case class Notification(activity: Activity)

  case class WebSocketConnect(actor: ActorRef)

  case class WebSocketDisconnect(actor: ActorRef)

}

class UserActor extends Actor with ActorLogging {

  var webSocketActors: Set[ActorRef] = Set.empty

  override def receive = {
    case WebSocketConnect(actor) =>
      log.info(s"WebSocket connected for ${context.self}: $actor")
      webSocketActors += actor
    case WebSocketDisconnect(actor) =>
      log.info(s"WebSocket disconnected for ${context.self}: $actor")
      webSocketActors -= actor
    case Notification(activity) =>
      log.info(s"Delivering notification ${activity.id} to ${webSocketActors.size} connected WebSocket(s)")
      webSocketActors.foreach(ws => ws ! Notification(activity))
    case _ =>
  }

}
