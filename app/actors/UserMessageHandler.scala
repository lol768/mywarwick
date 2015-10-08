package actors

import actors.WebsocketActor.{ClientDataWrapper, ClientData}
import akka.actor.{ActorLogging, Actor}
import play.api.libs.json.Json

class UserMessageHandler extends Actor with ActorLogging {
  override def receive: Receive = {
    case ClientDataWrapper(sender, clientData) => {
      // FIXME obviously don't have all the tile-specific handling here.
      if (clientData.tileId == "glob") {
        log.info("Well lookee here, if it isn't the people search")
        val name = (clientData.data \ "name").as[String]
        sender ! Json.obj(
          "tileId" -> clientData.tileId,
          "type" -> "tile-update",
          "replyTo" -> clientData.messageId,
          "data" -> Json.obj(
            "count" -> 2,
            "items" -> Json.arr(
              Json.obj(
                "name" -> s"${name} Johnson",
                "email" -> s"${name.charAt(0).toUpper}.Johnson.9999@warwick.ac.uk"
              ),
              Json.obj(
                "name" -> s"${name} Jakobsen",
                "email" -> s"${name.charAt(0).toUpper}.Jacobsen.9999@warwick.ac.uk"
              )
            )
          )
        )
      }
    }
  }
}
