package actors

import actors.WebsocketActor.ClientDataWrapper
import akka.actor.{Actor, ActorLogging}
import play.api.libs.json.{JsString, JsObject}

class UserMessageHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case ClientDataWrapper(sender, clientData) => {
      // FIXME obviously don't have all the tile-specific handling here.

      if ((clientData.data \ "type").as[String] == "get-current-notifications") {
        sender ! JsObject(Seq(
          "key" -> JsString("999999"),
          "text" -> JsString("This is the notification you asked for"),
          "source" -> JsString("Tabula"),
          "date" -> JsString("2015-10-14T12:00")
        ))
      }

    }
  }
}
