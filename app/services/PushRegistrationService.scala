package services

import com.google.inject.{ImplementedBy, Inject}
import models.Platform
import play.api.db.{Database, NamedDatabase}
import services.dao.PushRegistrationDao
import warwick.sso.Usercode

@ImplementedBy(classOf[PushRegistrationServiceImpl])
trait PushRegistrationService {
  def save(usercode: Usercode, platform: Platform, token: String): Boolean

  def remove(token: String): Boolean
}

class PushRegistrationServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  pushRegistrationDao: PushRegistrationDao
) extends PushRegistrationService {

  override def save(usercode: Usercode, platform: Platform, token: String): Boolean =
    db.withConnection { implicit c =>
      pushRegistrationDao.saveRegistration(usercode, platform, token)
    }

  override def remove(token: String): Boolean =
    db.withConnection { implicit c =>
      pushRegistrationDao.removeRegistration(token)
    }

}
