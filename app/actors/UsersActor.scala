package actors

import actors.UsersActor.MessageToUser
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import warwick.sso.Usercode

object UsersActor {

  case class MessageToUser(usercode: Usercode, message: AnyRef)

}

class UsersActor extends Actor with ActorLogging {

  def receive = {
    case MessageToUser(usercode, message) =>
      actorForUser(usercode) ! message
  }

  def actorForUser(usercode: Usercode): ActorRef =
    context.child(usercode.string)
      .getOrElse(context.actorOf(Props[UserActor], usercode.string))

}
