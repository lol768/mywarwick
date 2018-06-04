package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.notnoop.apns.APNS
import javax.inject.{Inject, Named}
import models.Platform._
import models.{MessageSend, PushRegistration}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityDao, PublisherDao, PushRegistrationDao}
import warwick.sso.Usercode

import scala.concurrent.{ExecutionContext, Future}

@Named("apns")
class APNSOutputService @Inject()(
  @NamedDatabase("default") db: Database,
  apnsProvider: APNSProvider,
  pushRegistrationDao: PushRegistrationDao,
  activityDao: ActivityDao,
  publisherDao: PublisherDao
)(implicit @Named("mobile") ec: ExecutionContext) extends MobileOutputService {

  import apnsProvider.apns

  val notificationSound: String = "Alert.wav"

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] =
    db.withConnection { implicit c =>
      publisherDao.getProvider(message.activity.providerId) match {
        case Some(provider) if provider.overrideMuting =>
          send(message.user.usercode, MobileOutputService.toPushNotification(message.activity, Some(Priority.HIGH)))
        case _ =>
          send(message.user.usercode, MobileOutputService.toPushNotification(message.activity))
      }
    }

  def processPushNotification(usercodes: Set[Usercode], pushNotification: PushNotification): Future[ProcessingResult] =
    Future.sequence(usercodes.map(send(_, pushNotification))).map(_ => ProcessingResult(success = true, "ok"))

  def send(usercode: Usercode, pushNotification: PushNotification): Future[ProcessingResult] = Future {
    val payload = makePayload(
      title = pushNotification.buildTitle(Emoji.LINK),
      badge = getUnreadNotificationCount(usercode),
      priority = pushNotification.priority.getOrElse(Priority.NORMAL),
      transient = pushNotification.transient,
    )

    getApplePushRegistrations(usercode).foreach(device => apns.push(device.token, payload))

    ProcessingResult(success = true, message = s"Push notification(s) sent")
  }

  private def makePayload(title: String, badge: Int, priority: Priority, transient: Boolean): String =
    APNS.newPayload()
      .alertBody(title)
      .badge(badge)
      .sound(notificationSound)
      .customField("priority", priority.value)
      .customField("transient", transient)
      .build()

  def getUnreadNotificationCount(usercode: Usercode): Int = db.withConnection { implicit c =>
    val lastReadNotification = activityDao.getLastReadDate(usercode.string).getOrElse(new DateTime(0))

    activityDao.countNotificationsSinceDate(usercode.string, lastReadNotification)
  }

  def getApplePushRegistrations(usercode: Usercode): Seq[PushRegistration] = db.withConnection { implicit c =>
    pushRegistrationDao.getPushRegistrationsForUser(usercode)
      .filter(_.platform == Apple)
  }

  override def clearUnreadCount(usercode: Usercode): Unit =
    getApplePushRegistrations(usercode).foreach(device => apns.push(device.token, zeroBadgePayload))

  val zeroBadgePayload = APNS.newPayload().badge(0).build()

}
