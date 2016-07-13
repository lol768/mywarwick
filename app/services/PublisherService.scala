package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.{Publisher, PublishingRole}
import play.api.db.{Database, NamedDatabase}
import services.dao.PublisherDao
import warwick.sso.Usercode

@ImplementedBy(classOf[PublisherServiceImpl])
trait PublisherService {

  def all: Seq[Publisher]

  def find(id: String): Option[Publisher]

  def getRolesForUser(id: String, usercode: Usercode): Seq[PublishingRole]

}

@Singleton
class PublisherServiceImpl @Inject()(
  publisherDao: PublisherDao,
  @NamedDatabase("default") db: Database
) extends PublisherService {

  override def all = db.withConnection(implicit c => publisherDao.all)

  override def find(id: String) = db.withConnection(implicit c => publisherDao.find(id))

  override def getRolesForUser(id: String, usercode: Usercode) = db.withConnection { implicit c =>
    publisherDao.getPublisherPermissions(id, usercode).map(_.role)
  }
}
