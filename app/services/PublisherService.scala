package services

import java.sql.Connection

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.publishing.PublishingRole.Viewer
import models.publishing._
import play.api.db.{Database, NamedDatabase}
import services.dao.PublisherDao
import warwick.sso.Usercode

case class Provider(id: String, name: String)

@ImplementedBy(classOf[PublisherServiceImpl])
trait PublisherService {

  def all: Seq[Publisher]

  def find(id: String): Option[Publisher]

  def getRoleForUser(publisherId: String, usercode: Usercode): Role

  def getPermissionScope(publisherId: String): PermissionScope

  def getPublishersForUser(usercode: Usercode): Seq[Publisher]

  def getParentPublisherId(providerId: String): Option[String]

  def getProviders(publisherId: String): Seq[Provider]

  def isPublisher(usercode: Usercode): Boolean

  def getPublisherPermissions(publisherId: String): Seq[PublisherPermission]

  def partitionGlobalPublishers(publishers: Seq[Publisher]): (Seq[Publisher], Seq[Publisher])

  def save(publisher: Publisher): String

}

@Singleton
class PublisherServiceImpl @Inject()(
  dao: PublisherDao,
  @NamedDatabase("default") db: Database
) extends PublisherService {

  val AllDepartmentsWildcard = "**"

  override def all: Seq[Publisher] = db.withConnection(implicit c => dao.all)

  override def find(id: String): Option[Publisher] = db.withConnection(implicit c => dao.find(id))

  override def getRoleForUser(publisherId: String, usercode: Usercode): CompoundRole = db.withConnection { implicit c =>
    val explicitRoles = dao.getPublisherPermissions(publisherId, usercode).map(_.role)
    if (hasGlobalPermission(dao.getPublishersForUser(usercode))) {
      CompoundRole(explicitRoles ++ Seq(Viewer))
    } else {
      CompoundRole(explicitRoles)
    }
  }

  override def getPermissionScope(publisherId: String): PermissionScope = db.withConnection { implicit c =>
    val departments = dao.getPublisherDepartments(publisherId)

    if (departments.contains(AllDepartmentsWildcard)) {
      PermissionScope.AllDepartments
    } else {
      PermissionScope.Departments(departments)
    }
  }

  override def getParentPublisherId(providerId: String): Option[String] = db.withConnection { implicit c =>
    dao.getParentPublisherId(providerId)
  }

  override def getProviders(publisherId: String): Seq[Provider] = db.withConnection { implicit c =>
    dao.getProviders(publisherId)
  }

  private def hasGlobalPermission(userPublishers: Seq[Publisher])(implicit c: Connection): Boolean =
    userPublishers.exists(p => dao.getPublisherDepartments(p.id).contains(AllDepartmentsWildcard))

  override def getPublishersForUser(usercode: Usercode): Seq[Publisher] = db.withConnection(implicit c => {
    val explicitPublishers = dao.getPublishersForUser(usercode)
    if (hasGlobalPermission(explicitPublishers)) {
      dao.all
    } else {
      explicitPublishers
    }
  }).sortBy(_.name)

  override def isPublisher(usercode: Usercode): Boolean = db.withConnection(implicit c => dao.isPublisher(usercode.string))

  override def getPublisherPermissions(publisherId: String): Seq[PublisherPermission] =
    db.withConnection(implicit c => dao.getAllPublisherPermissions(publisherId))

  override def partitionGlobalPublishers(publishers: Seq[Publisher]): (Seq[Publisher], Seq[Publisher]) = db.withConnection(implicit c =>
    publishers.partition(p => dao.getPublisherDepartments(p.id).contains(AllDepartmentsWildcard))
  )

  override def save(publisher: Publisher): String =
    db.withConnection(implicit c => dao.save(publisher))
}