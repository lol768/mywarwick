package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.{ImplementedBy, Inject}
import com.google.inject.name.Named
import models.MessageSend
import warwick.sso.Usercode

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[MobileOutputServiceImpl])
trait MobileOutputService extends OutputService {
  def clearUnreadCount(user: Usercode): Unit
}

@Named("mobile")
class MobileOutputServiceImpl @Inject()(
  apns: APNSOutputService,
  fcm: FCMOutputService,
  webPush: WebPushOutputService
) extends MobileOutputService {

  import system.ThreadPools.mobile

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    Future.sequence(Seq(
      apns.send(message),
      fcm.send(message),
      webPush.send(message)
    )).map(_ => ProcessingResult(success = true, "perfect"))
  }

  override def clearUnreadCount(user: Usercode): Unit = {
    apns.clearUnreadCount(user)
  }
}
