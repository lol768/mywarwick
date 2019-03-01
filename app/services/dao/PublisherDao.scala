package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.publishing.{Publisher, PublisherPermission, PublisherSave, PublishingRole}
import services.{ProviderRender, ProviderSave, PublisherService}
import warwick.sso.Usercode
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[PublisherDaoImpl])
trait PublisherDao {

  def all(implicit c: Connection): Seq[Publisher]

  def find(id: String)(implicit c: Connection): Option[Publisher]

  def getPublisherPermissions(publisherId: String, usercode: Usercode)(implicit c: Connection): Seq[PublisherPermission]

  def getAllPublisherPermissions(publisherId: String)(implicit c: Connection): Seq[PublisherPermission]

  def getPublisherDepartments(publisherId: String)(implicit c: Connection): Seq[String]

  def getPublishersForUser(usercode: Usercode)(implicit c: Connection): Seq[Publisher]

  def getParentPublisherId(providerId: String)(implicit c: Connection): Option[String]

  def getProviders(publisherId: String)(implicit c: Connection): Seq[ProviderRender]

  def getAllProviders()(implicit c: Connection): Seq[ProviderRender]

  def isPublisher(usercode: String)(implicit  c: Connection): Boolean

  def save(id: String, data: PublisherSave)(implicit c: Connection): String

  def update(id: String, data: PublisherSave)(implicit c: Connection): Unit

  def updatePermissionScope(publisherId: String, isAllDepartments: Boolean, departmentCodes: Seq[String])(implicit c: Connection): Unit

  def updatePublisherPermissions(publisherId: String, permissions: Seq[PublisherPermission])(implicit c: Connection): Unit

  def saveProvider(publisherId: String, providerId: String, data: ProviderSave)(implicit c: Connection): String

  def updateProvider(publisherId: String, providerId: String, data: ProviderSave)(implicit c: Connection): Unit

  def getProvider(providerId: String)(implicit c: Connection): Option[ProviderRender]

}

@Singleton
class PublisherDaoImpl extends PublisherDao {

  val publisherParser: RowParser[Publisher] =
    str("id") ~ str("name") ~ get[Option[Int]]("max_recipients") map {
      case id ~ name ~ maxRecipients => Publisher(id, name, maxRecipients)
    }

  val publisherPermissionParser: RowParser[PublisherPermission] = str("usercode") ~ str("role") map {
    case usercode ~ role => PublisherPermission(Usercode(usercode), PublishingRole.withName(role))
  }

  var providerParser: RowParser[ProviderRender] =
    str("id") ~
    get[Option[String]]("display_name") ~
    get[Option[String]]("icon") ~
    get[Option[String]]("colour") ~
    bool("send_email") ~
    bool("override_muting") map {
      case id ~ name ~ icon ~ colour ~ sendEmail ~ overrideMuting =>
        ProviderRender(id, name, icon, colour, sendEmail, overrideMuting)
    }

  override def getPublishersForUser(usercode: Usercode)(implicit c: Connection): Seq[Publisher] =
    SQL"SELECT DISTINCT PUBLISHER.* FROM PUBLISHER JOIN PUBLISHER_PERMISSION ON PUBLISHER_PERMISSION.PUBLISHER_ID = PUBLISHER.ID WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(publisherParser.*)

  override def all(implicit c: Connection): Seq[Publisher] =
    SQL"SELECT * FROM PUBLISHER"
      .executeQuery()
      .as(publisherParser.*)

  override def find(id: String)(implicit c: Connection): Option[Publisher] =
    SQL"SELECT * FROM PUBLISHER WHERE ID = $id"
      .executeQuery()
      .as(publisherParser.singleOpt)

  override def getPublisherPermissions(publisherId: String, usercode: Usercode)(implicit c: Connection): Seq[PublisherPermission] =
    SQL"SELECT * FROM PUBLISHER_PERMISSION WHERE PUBLISHER_ID = $publisherId AND USERCODE = ${usercode.string}"
      .executeQuery()
      .as(publisherPermissionParser.*)

  override def getAllPublisherPermissions(publisherId: String)(implicit c: Connection): Seq[PublisherPermission] =
    SQL"SELECT * FROM PUBLISHER_PERMISSION WHERE PUBLISHER_ID = $publisherId"
      .executeQuery()
      .as(publisherPermissionParser.*)

  override def getPublisherDepartments(publisherId: String)(implicit c: Connection): Seq[String] =
    SQL"SELECT DEPARTMENT_CODE FROM PUBLISHER_DEPARTMENT WHERE PUBLISHER_ID = $publisherId"
      .executeQuery()
      .as(scalar[String].*)

  override def getParentPublisherId(providerId: String)(implicit c: Connection): Option[String] =
    SQL"SELECT p.id FROM publisher p JOIN provider pr ON p.id = pr.publisher_id WHERE pr.id = $providerId"
      .executeQuery()
      .as(scalar[String].singleOpt)

  override def getProviders(publisherId: String)(implicit c: Connection): Seq[ProviderRender] =
    SQL"SELECT * FROM provider WHERE publisher_id = $publisherId ORDER BY display_name"
      .executeQuery()
      .as(providerParser.*)

  override def getAllProviders()(implicit c: Connection): Seq[ProviderRender] =
    SQL"select * from PROVIDER"
      .executeQuery()
      .as(providerParser.*)

  override def isPublisher(usercode: String)(implicit c: Connection): Boolean =
    SQL"SELECT COUNT(*) FROM PUBLISHER_PERMISSION WHERE USERCODE = $usercode"
      .executeQuery()
      .as(scalar[Int].single) > 0

  override def save(id: String, data: PublisherSave)(implicit c: Connection): String = {
    import data._
    SQL"""
      INSERT INTO PUBLISHER (id, name, max_recipients)
      VALUES ($id, $name, $maxRecipients)
    """.executeUpdate()
    id
  }

  override def update(id: String, data: PublisherSave)(implicit c: Connection): Unit = {
    import data._
    SQL"""
      UPDATE PUBLISHER SET name = $name, max_recipients = $maxRecipients
      WHERE id = $id
    """.execute()
  }

  override def updatePermissionScope(publisherId: String, isAllDepartments: Boolean, departmentCodes: Seq[String])(implicit c: Connection): Unit = {
    SQL"DELETE FROM PUBLISHER_DEPARTMENT WHERE publisher_id = $publisherId".execute()
    if (isAllDepartments) {
      SQL"INSERT INTO PUBLISHER_DEPARTMENT VALUES ($publisherId, ${PublisherService.AllDepartmentsWildcard})".executeUpdate()
    } else {
      departmentCodes.foreach(deptCode =>
        SQL"INSERT INTO PUBLISHER_DEPARTMENT VALUES ($publisherId, $deptCode)".executeUpdate()
      )
    }
  }

  override def updatePublisherPermissions(publisherId: String, permissions: Seq[PublisherPermission])(implicit c: Connection): Unit = {
    SQL"DELETE FROM PUBLISHER_PERMISSION WHERE publisher_id = $publisherId".execute()
    permissions.foreach(permission =>
      SQL"INSERT INTO PUBLISHER_PERMISSION VALUES ($publisherId, ${permission.usercode.string}, ${permission.role.toString})".executeUpdate()
    )
  }

  override def saveProvider(publisherId: String, providerId: String, data: ProviderSave)(implicit c: Connection): String = {
    import data._
    SQL"""
      INSERT INTO PROVIDER (id, display_name, icon, colour, publisher_id, send_email, override_muting)
      VALUES ($providerId, $name, $icon, $colour, $publisherId, $sendEmail, $overrideMuting)
    """.executeUpdate()
    providerId
  }

  override def updateProvider(publisherId: String, providerId: String, data: ProviderSave)(implicit c: Connection): Unit = {
    import data._
    SQL"""
      UPDATE PROVIDER SET display_name = $name, icon = $icon, colour = $colour, send_email = $sendEmail, override_muting = $overrideMuting
      WHERE id = $providerId and PUBLISHER_ID = $publisherId
    """.executeUpdate()
  }

  override def getProvider(providerId: String)(implicit c: Connection): Option[ProviderRender] =
    SQL"SELECT * FROM PROVIDER WHERE ID = $providerId".executeQuery().as(providerParser.singleOpt)
}


