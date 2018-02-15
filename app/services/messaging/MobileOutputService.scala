package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.name.Named
import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, ActivitySave, MessageSend}
import warwick.sso.Usercode

import scala.concurrent.Future

case class PushNotification(title: String, text: Option[String], url: Option[String])

object MobileOutputService {
  def toPushNotification(activity: ActivitySave): PushNotification =
    PushNotification(activity.title, activity.text, activity.url)
  def toPushNotification(activity: Activity): PushNotification =
    PushNotification(activity.title, activity.text, activity.url)
}

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
