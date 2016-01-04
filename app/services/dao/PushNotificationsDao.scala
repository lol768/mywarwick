package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.{TileConfig, TileSize, Tile, TileInstance}
import play.api.libs.json.{JsObject, Json}
import services.messaging.PushNotificationsService

@ImplementedBy(classOf[PushNotificationsDaoImpl])
trait PushNotificationsDao {

  case class PushRegistration(registrationId: String, platform: String)

  object PushRegistration {
    implicit val reads = Json.reads[PushRegistration]
  }

  def saveSubscription(usercode: String, platform: String, regId: String)(implicit c: Connection): Boolean

  def registrationExists(regId: String)(implicit c: Connection): Boolean

  def getRegistrationsFor(usercode: String)(implicit c: Connection): Seq[PushRegistration]
}

class PushNotificationsDaoImpl @Inject()(
) extends PushNotificationsDao {

  def pushRegistrationParser: RowParser[PushRegistration] = {
    get[String]("registration_id") ~
      get[String]("platform") map {
      case regId ~ platform => PushRegistration(regId, platform)
    }
  }
  override def getRegistrationsFor(usercode: String)(implicit c: Connection): Seq[PushRegistration] = {
    SQL("SELECT registration_id, platform FROM push_subscription WHERE usercode = {usercode}")
      .on('usercode -> usercode)
      .as(pushRegistrationParser.*)
  }

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
      true
    }
  }


}
