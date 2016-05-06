package actors

import actors.WebsocketActor.ClientDataWrapper
import akka.actor.{Actor, ActorLogging, Props}
import play.api.Logger
import warwick.sso.LoginContext

object UserMessageHandler {
  def props(loginContext: LoginContext): Props = Props(classOf[UserMessageHandler], loginContext)
}

class UserMessageHandler(loginContext: LoginContext) extends Actor with ActorLogging {

  val logger = Logger(getClass)

  override def receive: Receive = {
    case ClientDataWrapper(sender, clientData) => {
      val msgType = (clientData.data \ "type").as[String]

      logger.debug(s"Received message type $msgType")
    }
  }

}
