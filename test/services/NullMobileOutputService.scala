package services

import actors.MessageProcessing.ProcessingResult
import models.MessageSend
import services.messaging.{MobileOutputService, PushNotification}
import warwick.sso.Usercode

import scala.concurrent.Future

class NullMobileOutputService extends MobileOutputService {
  override def send(message: MessageSend.Heavy): Future[ProcessingResult] =
    Future.failed(new UnsupportedOperationException)

  override def clearUnreadCount(user: Usercode): Unit = {}

  override def processPushNotification(usercodes: Set[Usercode], pushNotification: PushNotification) =
    Future.failed(new UnsupportedOperationException)
}
