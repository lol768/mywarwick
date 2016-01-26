package actors

import actors.WebsocketActor.ClientDataWrapper
import akka.actor.{Actor, ActorLogging, Props}
import play.api.Logger
import play.api.libs.json._
import warwick.sso.LoginContext

object UserMessageHandler {
  def props(loginContext: LoginContext): Props = Props(classOf[UserMessageHandler], loginContext)
}


class UserMessageHandler(loginContext: LoginContext) extends Actor with ActorLogging {

  val logger = Logger(getClass)

  override def receive: Receive = {
    case ClientDataWrapper(sender, clientData) => {
      // FIXME obviously don't have all the tile-specific handling here.

      val msgType = (clientData.data \ "type").as[String]

      logger.debug(s"Received message type ${msgType}")

      msgType match {
        case "who-am-i" =>
          sender ! JsObject(Seq(
            "type" -> JsString("who-am-i"),
            "userIdentity" -> userInfo(loginContext)
          ))
      }

    }
  }

  def userInfo(context: LoginContext) = {
    context.user.map { user =>
      Json.obj(
        "authenticated" -> true,
        "usercode" -> user.usercode.string,
        "name" -> user.name.full,
        "masquerading" -> context.isMasquerading
      )
    }.getOrElse(Json.obj("authenticated" -> false))
  }
}
