package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.google.inject.name.Named
import com.notnoop.apns.APNS
import models.Platform._
import models.{MessageSend, PushRegistration}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityDao, PushRegistrationDao}
import warwick.sso.Usercode

import scala.concurrent.Future

@Named("apns")
class APNSOutputService @Inject()(
  @NamedDatabase("default") db: Database,
  apnsProvider: APNSProvider,
  pushRegistrationDao: PushRegistrationDao,
  activityDao: ActivityDao
) extends MobileOutputService {

  import apnsProvider.apns
  import system.ThreadPools.mobile

  val notificationSound: String = "Alert.wav"

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] =
    send(message.user.usercode, MobileOutputService.toPushNotification(message.activity))

  def processPushNotification(usercodes: Set[Usercode], pushNotification: PushNotification): Future[ProcessingResult] =
    Future.sequence(usercodes.map(send(_, pushNotification))).map(_ => ProcessingResult(success = true, "ok"))

  def send(usercode: Usercode, pushNotification: PushNotification): Future[ProcessingResult] = Future {
    val payload = makePayload(
      title = pushNotification.buildTitle(Emoji.LINK),
      badge = getUnreadNotificationCount(usercode),
      sound = pushNotification.apnsSound.getOrElse(notificationSound),
      priority = pushNotification.priority.getOrElse(Priority.NORMAL)
    )

    getApplePushRegistrations(usercode).foreach(device => apns.push(device.token, payload))

    ProcessingResult(success = true, message = s"Push notification(s) sent")
  }

  private def makePayload(title: String, badge: Int, sound: String, priority: Priority): String = {
    APNS.newPayload()
      .alertBody(title)
      .badge(badge)
      .sound(sound)
      .customField("priority", priority.value)
      .build()
  }

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
