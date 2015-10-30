package actors

import actors.WebsocketActor.ClientDataWrapper
import akka.actor.{Props, Actor, ActorLogging}
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
        case "fetch-notifications" =>
          sender ! JsObject(Seq(
            "type" -> JsString("fetch-notifications"),
            "notifications" -> JsArray(
              Seq(JsObject(Seq(
                "id" -> JsString("999"),
                "text" -> JsString("This notification happened since you last logged in"),
                "source" -> JsString("Tabula"),
                "date" -> JsString("2015-10-14T15:00")
              )),
                JsObject(Seq(
                  "id" -> JsString("998"),
                  "text" -> JsString("This notification happened since you last logged in"),
                  "source" -> JsString("Tabula"),
                  "date" -> JsString("2015-10-17T18:00")
                )),
                JsObject(Seq(
                  "id" -> JsString("997"),
                  "text" -> JsString("This notification happened since you last logged in"),
                  "source" -> JsString("Tabula"),
                  "date" -> JsString("2015-10-18T12:00")
                ))
              )
            )
          ))
        case "who-am-i" =>
          sender ! JsObject(Seq(
            "type" -> JsString("who-am-i"),
            "notice" -> JsString("This is very not-production"),
            "user-info" -> userInfo(loginContext)
          ))
      }

    }
  }

  def userInfo(loginContext: LoginContext) = loginContext.user.filter(_.isFound).map { user =>
    JsObject(Seq(
      "authenticated" -> JsBoolean(true),
      "usercode" -> JsString(user.usercode.string),
      "name" -> JsString(user.name.full.orNull)
    ))
  }.getOrElse {
    JsObject(Seq(
      "authenticated" -> JsBoolean(false)
    ))
  }
}
