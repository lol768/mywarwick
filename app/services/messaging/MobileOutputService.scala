package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import models.{Activity, MessageSend}
import system.Logging
import warwick.sso.Usercode

import scala.concurrent.{ExecutionContext, Future}

object MobileOutputService {
  def toPushNotification(activity: Activity, priority: Option[Priority] = Some(Priority.NORMAL), channelId: Option[String] = None): PushNotification =
    PushNotification(
      activity.id,
      Payload(activity.title, activity.text, activity.url),
      activity.publisherId,
      activity.providerId,
      activity.`type`,
      channel = channelId,
      priority = priority
    )
}

@ImplementedBy(classOf[MobileOutputServiceImpl])
trait MobileOutputService extends OutputService {
  def clearUnreadCount(user: Usercode): Unit
  def processPushNotification(usercodes: Set[Usercode], pushNotification: PushNotification): Future[ProcessingResult]
}

@Named("mobile")
class MobileOutputServiceImpl @Inject()(
  apns: APNSOutputService,
  fcm: FCMOutputService,
  webPush: WebPushOutputService
)(implicit @Named("mobile") ec: ExecutionContext) extends MobileOutputService with Logging {

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    Future.sequence(Seq(
      apns.send(message),
      fcm.send(message),
      webPush.send(message)
    )).map(_ => ProcessingResult(success = true, "perfect"))
  }

  override def processPushNotification(usercodes: Set[Usercode], pushNotification: PushNotification): Future[ProcessingResult] =
    Future.sequence(Seq(
      apns.processPushNotification(usercodes, pushNotification),
      fcm.processPushNotification(usercodes, pushNotification)
    )).map(_ => ProcessingResult(success = true, "perfect"))

  override def clearUnreadCount(user: Usercode): Unit = {
    apns.clearUnreadCount(user)
  }
}
