package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.publishing.{Publisher, PublisherPermission}
import models.publishing.PublishingRole
import warwick.sso.Usercode

@ImplementedBy(classOf[PublisherDaoImpl])
trait PublisherDao {

  def all(implicit c: Connection): Seq[Publisher]

  def find(id: String)(implicit c: Connection): Option[Publisher]

  def getPublisherPermissions(publisherId: String, usercode: Usercode)(implicit c: Connection): Seq[PublisherPermission]

  def getPublisherDepartments(publisherId: String)(implicit c: Connection): Seq[String]

}

@Singleton
class PublisherDaoImpl extends PublisherDao {

  val publisherParser = str("id") ~ str("name") map { case id ~ name => Publisher(id, name) }

  val publisherPermissionParser = str("usercode") ~ str("role") map { case usercode ~ role => PublisherPermission(Usercode(usercode), PublishingRole.withName(role)) }

  override def all(implicit c: Connection) =
    SQL"SELECT * FROM PUBLISHER"
      .executeQuery()
      .as(publisherParser.*)

  override def find(id: String)(implicit c: Connection) =
    SQL"SELECT * FROM PUBLISHER WHERE ID = $id"
      .executeQuery()
      .as(publisherParser.singleOpt)

  override def getPublisherPermissions(publisherId: String, usercode: Usercode)(implicit c: Connection) =
    SQL"SELECT * FROM PUBLISHER_PERMISSION WHERE PUBLISHER_ID = $publisherId AND USERCODE = ${usercode.string}"
      .executeQuery()
      .as(publisherPermissionParser.*)

  override def getPublisherDepartments(publisherId: String)(implicit c: Connection) =
    SQL"SELECT DEPARTMENT_CODE FROM PUBLISHER_DEPARTMENT WHERE PUBLISHER_ID = $publisherId"
      .executeQuery()
      .as(scalar[String].*)

}


