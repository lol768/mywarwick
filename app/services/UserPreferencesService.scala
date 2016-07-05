package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.db.{Database, NamedDatabase}
import services.dao.UserPreferencesDao
import warwick.sso.Usercode

@ImplementedBy(classOf[UserPreferencesServiceImpl])
trait UserPreferencesService {

  def exists(usercode: Usercode): Boolean

  def save(usercode: Usercode): Unit

}

@Singleton
class UserPreferencesServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: UserPreferencesDao
) extends UserPreferencesService {

  override def exists(usercode: Usercode) = db.withConnection(implicit c => dao.exists(usercode))

  override def save(usercode: Usercode) = db.withConnection(implicit c => dao.save(usercode))

}
