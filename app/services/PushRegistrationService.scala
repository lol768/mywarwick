package services

import com.google.inject.{ImplementedBy, Inject}
import models.Platform
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.PushRegistrationDao
import warwick.sso.Usercode

@ImplementedBy(classOf[PushRegistrationServiceImpl])
trait PushRegistrationService {
  def save(usercode: Usercode, platform: Platform, token: String, deviceString: String): Boolean

  def remove(token: String): Boolean

  def removeIfNotRegisteredSince(deviceToken: String, date: DateTime): Boolean
}

class PushRegistrationServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  pushRegistrationDao: PushRegistrationDao
) extends PushRegistrationService {

  override def save(usercode: Usercode, platform: Platform, token: String, deviceString: String): Boolean =
    db.withConnection { implicit c =>
      pushRegistrationDao.saveRegistration(usercode, platform, token, deviceString)
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
