package services

import javax.inject.Inject
import javax.inject.Singleton
import com.google.inject.ImplementedBy
import play.api.db.{Database, NamedDatabase}
import services.dao.EmailNotificationsPrefDao
import warwick.sso.Usercode

@ImplementedBy(classOf[EmailNotificationsPrefServiceImpl])
trait EmailNotificationsPrefService {

    def get(usercode: Usercode): Boolean

    def set(usercode: Usercode, wantsEmail: Boolean): Unit

}

@Singleton
class EmailNotificationsPrefServiceImpl @Inject()(
dao: EmailNotificationsPrefDao,
@NamedDatabase("default") db: Database
) extends EmailNotificationsPrefService {

  override def get(usercode: Usercode): Boolean = db.withConnection(implicit c => dao.getUserPreference(usercode))

  override def set(usercode: Usercode, wantsEmail: Boolean): Unit =
    db.withConnection(implicit c => dao.setUserPreference(usercode, wantsEmail))

}
