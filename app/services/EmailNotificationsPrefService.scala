package services

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.db.{Database, NamedDatabase}
import services.dao.UserPreferencesDao
import warwick.sso.Usercode

@ImplementedBy(classOf[EmailNotificationsPrefServiceImpl])
trait EmailNotificationsPrefService {

    def get(usercode: Usercode): Boolean

    def set(usercode: Usercode, wantsEmail: Boolean): Unit

}

@Singleton
class EmailNotificationsPrefServiceImpl @Inject()(
dao: UserPreferencesDao,
@NamedDatabase("default") db: Database
) extends EmailNotificationsPrefService {

  override def get(usercode: Usercode): Boolean =
    db.withConnection(implicit c => dao.getUserEmailsPreference(usercode))

  override def set(usercode: Usercode, wantsEmail: Boolean): Unit =
    db.withConnection(
      implicit c => dao.setUserEmailsPreference(usercode, wantsEmail)
    )
}
