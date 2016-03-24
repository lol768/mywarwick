package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.notnoop.apns.APNS
import models.Platform._
import models.{MessageSend, PushRegistration}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityDao, PushRegistrationDao}
import warwick.sso.Usercode

import scala.concurrent.Future

class APNSOutputService @Inject()(
  @NamedDatabase("default") db: Database,
  apnsProvider: APNSProvider,
  pushRegistrationDao: PushRegistrationDao,
  activityDao: ActivityDao
) extends OutputService {

  import apnsProvider.apns
  import system.ThreadPools.mobile

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = Future {
    import message._

    val payload = makePayload(
      title = activity.title,
      badge = getUnreadNotificationCount(user.usercode)
    )

    getApplePushRegistrations(user.usercode).foreach(device => apns.push(device.token, payload))

    ProcessingResult(success = true, message = s"Push notification(s) sent")
  }

  def makePayload(title: String, badge: Int): String = {
    APNS.newPayload()
      .alertBody(title)
      .badge(badge)
      .sound("Alert.wav")
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

  def clearAppIconBadge(usercode: Usercode): Unit = db.withConnection { implicit c =>
    getApplePushRegistrations(usercode).foreach(device => apns.push(device.token, zeroBadgePayload))
  }

  val zeroBadgePayload = APNS.newPayload().badge(0).build()

}
