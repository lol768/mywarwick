package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.Audience
import play.api.db.{Database, NamedDatabase}
import services.dao.UserNewsOptInDao
import warwick.sso.Usercode

@ImplementedBy(classOf[UserNewsOptInServiceImpl])
trait UserNewsOptInService {

  def get(usercode: Usercode): Seq[Audience.OptIn]

  def set(usercode: Usercode, optInType: String, optIns: Seq[Audience.OptIn]): Unit

}

@Singleton
class UserNewsOptInServiceImpl @Inject()(
  dao: UserNewsOptInDao,
  @NamedDatabase("default") db: Database
) extends UserNewsOptInService {

  override def get(usercode: Usercode): Seq[Audience.OptIn] =
    db.withConnection(implicit c => dao.get(usercode))

  override def set(usercode: Usercode, optInType: String, optIns: Seq[Audience.OptIn]): Unit =
    db.withTransaction(implicit c => dao.save(usercode, optInType, optIns))

}
