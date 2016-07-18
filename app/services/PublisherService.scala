package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.publishing._
import play.api.db.{Database, NamedDatabase}
import services.dao.PublisherDao
import warwick.sso.Usercode

@ImplementedBy(classOf[PublisherServiceImpl])
trait PublisherService {

  def all: Seq[Publisher]

  def find(id: String): Option[Publisher]

  def getRoleForUser(publisherId: String, usercode: Usercode): Role

  def getPermissionScope(publisherId: String): PermissionScope

  def getPublishersForUser(usercode: Usercode): Seq[Publisher]

}

@Singleton
class PublisherServiceImpl @Inject()(
  dao: PublisherDao,
  @NamedDatabase("default") db: Database
) extends PublisherService {

  val AllDepartmentsWildcard = "**"

  override def all = db.withConnection(implicit c => dao.all)

  override def find(id: String) = db.withConnection(implicit c => dao.find(id))

  override def getRoleForUser(publisherId: String, usercode: Usercode) = db.withConnection { implicit c =>
    CompoundRole(dao.getPublisherPermissions(publisherId, usercode).map(_.role))
  }

  override def getPermissionScope(publisherId: String) = db.withConnection { implicit c =>
    val departments = dao.getPublisherDepartments(publisherId)

    if (departments.contains(AllDepartmentsWildcard)) {
      PermissionScope.AllDepartments
    } else {
      PermissionScope.Departments(departments)
    }
  }

  override def getPublishersForUser(usercode: Usercode) = db.withConnection(implicit c => dao.getPublishersForUser(usercode))
}
