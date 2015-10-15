package actors

import actors.WebsocketActor.ClientDataWrapper
import akka.actor.{Actor, ActorLogging}
import play.api.libs.json._

class UserMessageHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case ClientDataWrapper(sender, clientData) => {
      // FIXME obviously don't have all the tile-specific handling here.

      // dummy response to client requesting notifications since last login
      if ((clientData.data \ "type").as[String] == "fetch-notifications") {
        sender ! JsObject(Seq(
        "type" -> JsString("fetch-notifications"),
          "notifications" -> JsArray(
            Seq(JsObject(Seq(
              "key" -> JsString("999"),
              "text" -> JsString("This notification happened since you last logged in"),
              "source" -> JsString("Tabula"),
              "date" -> JsString("2015-10-14T15:00")
            )),
              JsObject(Seq(
                "key" -> JsString("998"),
                "text" -> JsString("This notification happened since you last logged in"),
                "source" -> JsString("Tabula"),
                "date" -> JsString("2015-10-17T18:00")
              )),
              JsObject(Seq(
                "key" -> JsString("997"),
                "text" -> JsString("This notification happened since you last logged in"),
                "source" -> JsString("Tabula"),
                "date" -> JsString("2015-10-18T12:00")
              ))
            )
          )
        ))
      }
    }
  }
}
