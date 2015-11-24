package actors

import actors.UserActor.{Notification, WebSocketConnect, WebSocketDisconnect}
import akka.actor.ActorLogging
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.cluster.sharding.ShardRegion._
import akka.persistence.PersistentActor
import models.Activity
import warwick.sso.Usercode

object UserActor {

  case class Notification(usercode: Usercode, activity: Activity)

  case class WebSocketConnect(usercode: Usercode)

  case class WebSocketDisconnect(usercode: Usercode)

  val typeName = "UserActor"

  val extractIdentityId: ExtractEntityId = {
    case n@Notification(usercode, _) => (usercode.string, n)
    case e@WebSocketConnect(usercode) => (usercode.string, e)
    case e@WebSocketDisconnect(usercode) => (usercode.string, e)
  }

  val extractShardId: ExtractShardId = {
    case Notification(usercode, _) => shardId(usercode)
    case WebSocketConnect(usercode) => shardId(usercode)
    case WebSocketDisconnect(usercode) => shardId(usercode)
  }

  private val numberOfShards = 100

  private def shardId(usercode: Usercode): String =
    (math.abs(usercode.string.hashCode) % numberOfShards).toString
}

class UserActor extends PersistentActor with ActorLogging {

  val mediator = DistributedPubSub(context.system).mediator

  var messageQueue: Seq[Notification] = Seq.empty

  var connectedWebSocketCount = 0

  def anyConnectedWebSockets = connectedWebSocketCount > 0

  def webSocketTopic(usercode: Usercode) = s"user:${usercode.string}:websocket"

  override def receiveCommand = {
    case notification@Notification(usercode, _) =>
      if (anyConnectedWebSockets) {
        log.debug("Publishing notification to WebSocket")
        mediator ! Publish(webSocketTopic(usercode), notification)
      } else {
        log.debug("No WebSocket connected; adding received notification to queue")
        persist(notification) { n =>
          messageQueue = messageQueue ++ Seq(n)
        }
      }

    case command@WebSocketConnect(usercode) =>
      log.debug(s"WebSocketConnect for ${usercode.string}")
      persist(command) { _ =>
        connectedWebSocketCount += 1
        messageQueue.foreach(notification => mediator ! Publish(webSocketTopic(usercode), notification))
        messageQueue = Seq.empty
      }

    case command@WebSocketDisconnect(usercode) =>
      log.debug(s"WebSocketDisconnect for ${usercode.string}")

      persist(command) { _ =>
        connectedWebSocketCount -= 1
      }
  }

  override def receiveRecover = {
    case n: Notification =>
      messageQueue = messageQueue ++ Seq(n)
    case WebSocketConnect =>
      connectedWebSocketCount += 1
    case WebSocketDisconnect =>
      connectedWebSocketCount -= 1
  }

  override def persistenceId: String = s"user-${self.path.name}"
}
