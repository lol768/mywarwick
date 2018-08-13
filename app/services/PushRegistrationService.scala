package services

import com.google.inject.{ImplementedBy, Inject}
import models.Platform
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.PushRegistrationDao
import system.Logging
import warwick.sso.Usercode

object PushRegistrationService {
  val blacklisted = "BLACKLISTED"
}

@ImplementedBy(classOf[PushRegistrationServiceImpl])
trait PushRegistrationService {
  def save(usercode: Usercode, platform: Platform, token: String, deviceString: Option[String]): Boolean

  def remove(token: String): Boolean

  def removeIfNotRegisteredSince(deviceToken: String, date: DateTime): Boolean
}

class PushRegistrationServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  pushRegistrationDao: PushRegistrationDao
) extends PushRegistrationService with Logging {

  override def save(usercode: Usercode, platform: Platform, token: String, deviceString: Option[String]): Boolean = {
    if (token != PushRegistrationService.blacklisted) {
      db.withConnection { implicit c =>
        pushRegistrationDao.saveRegistration(usercode, platform, token, deviceString)
      }
    } else {
      logger.error(s"Not saving push registration for ${usercode.string} as it is blacklisted")
      false
    }
  }

  override def remove(token: String): Boolean =
    db.withConnection { implicit c =>
      pushRegistrationDao.removeRegistration(token)
    }

  override def removeIfNotRegisteredSince(token: String, date: DateTime): Boolean =
    db.withConnection { implicit c =>
      pushRegistrationDao.removeRegistrationIfNotRegisteredSince(token, date)
    }

}
