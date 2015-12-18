package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import services.PushNotificationsService

@ImplementedBy(classOf[PushNotificationsDaoImpl])
trait PushNotificationsDao {
  def saveSubscription(usercode: String, platform: String, regId: String)(implicit c: Connection): Boolean

  def registrationExists(regId: String)(implicit c: Connection): Boolean
}

class PushNotificationsDaoImpl @Inject()(
  pushNotificationsService: PushNotificationsService
) extends PushNotificationsDao {

  override def registrationExists(regId: String)(implicit c: Connection): Boolean = {
    SQL("SELECT COUNT(*) FROM push_subscription WHERE registration_id = {regId}")
      .on(
        'regId -> regId
      ).as(scalar[Int].single) > 0
  }

  override def saveSubscription(usercode: String, platform: String, regId: String)(implicit c: Connection): Boolean = {
    if (!registrationExists(regId)) {
      SQL("INSERT INTO PUSH_SUBSCRIPTION (registration_id, usercode, platform) VALUES ({regId}, {usercode}, {platform})")
        .on(
          'regId -> regId,
          'usercode -> usercode,
          'platform -> platform
        )
        .execute()
    } else {
      pushNotificationsService.sendNotification(regId, usercode)
      true
    }
  }
}
