package actors

import actors.WebsocketActor.ClientDataWrapper
import akka.actor.{Actor, ActorLogging}
import play.api.libs.json._

class UserMessageHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case ClientDataWrapper(sender, clientData) => {
      // FIXME obviously don't have all the tile-specific handling here.

      case class Message(key: String, text: String, source: String, date: String)
      implicit object MessageFormat extends Format[Message] {
        def writes(message: Message): JsValue = {
          val msgseq = Seq(
            "key" -> JsString(message.key),
            "text" -> JsString(message.text),
            "source" -> JsString(message.source),
            "date" -> JsString(message.date)
          )
          JsObject(msgseq)
        }

        def reads(json: JsValue): JsResult[Message] = {
          JsSuccess(Message("", "", "", ""))
        }

      }


      if ((clientData.data \ "type").as[String] == "fetch-notifications") {
        val dummyFetchNotification = List(
          Message("999", "This is fetched 1", "Tabula", "2015-10-18T14:00"),
          Message("998", "This is fetched 2", "Sitebuilder", "2015-10-17T21:00"),
          Message("997", "This is fetched 3", "Moodle", "2015-10-15T01:00")
        )

        sender ! JsObject(Seq(
          "notifications" -> Json.toJson(dummyFetchNotification)
        ))
      }

    }
  }
}
