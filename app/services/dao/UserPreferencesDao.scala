package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[UserPreferencesDaoImpl])
trait UserPreferencesDao {

  def exists(usercode: Usercode)(implicit c: Connection): Boolean

  def save(usercode: Usercode)(implicit c: Connection): Unit

  def countInitialisedUsers(usercodes: Seq[Usercode])(implicit c: Connection): Int

  def allInitialisedUsers()(implicit c: Connection): Seq[Usercode]

  def getNotificationFilter(usercode: Usercode)(implicit c: Connection): JsObject

  def getActivityFilter(usercode: Usercode)(implicit c: Connection): JsObject

  def setNotificationFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit

  def setActivityFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit

}

@Singleton
class UserPreferencesDaoImpl extends UserPreferencesDao {

  override def exists(usercode: Usercode)(implicit c: Connection): Boolean =
    SQL"SELECT CREATED_AT FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(scalar[DateTime].singleOpt)
      .nonEmpty

  override def save(usercode: Usercode)(implicit c: Connection): Unit =
    SQL"INSERT INTO USER_PREFERENCE (USERCODE, CREATED_AT) VALUES (${usercode.string}, SYSDATE)"
      .execute()

  override def countInitialisedUsers(usercodes: Seq[Usercode])(implicit c: Connection): Int =
    usercodes.grouped(1000).map { groupedUsercodes =>
      SQL"SELECT COUNT(*) FROM USER_PREFERENCE WHERE USERCODE IN (${groupedUsercodes.map(_.string)})"
        .as(scalar[Int].single)
    }.sum

  override def allInitialisedUsers()(implicit c: Connection): Seq[Usercode] =
    SQL"SELECT USERCODE FROM USER_PREFERENCE"
      .executeQuery()
      .as(str("usercode").*)
      .map(Usercode)

  override def getNotificationFilter(usercode: Usercode)(implicit c: Connection): JsObject =
    SQL"SELECT NOTIFICATION_FILTER FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(get[Option[String]]("notification_filter").singleOpt.map(
        _.flatten.map(Json.parse(_).as[JsObject]).getOrElse(JsObject(Nil))
      ))

  override def getActivityFilter(usercode: Usercode)(implicit c: Connection): JsObject =
    SQL"SELECT ACTIVITY_FILTER FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(get[Option[String]]("activity_filter").singleOpt.map(
        _.flatten.map(Json.parse(_).as[JsObject]).getOrElse(JsObject(Nil))
      ))

  def setNotificationFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit =
    SQL"UPDATE USER_PREFERENCE SET NOTIFICATION_FILTER = ${filter.toString} WHERE USERCODE = ${usercode.string}".execute()

  def setActivityFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit =
    SQL"UPDATE USER_PREFERENCE SET ACTIVITY_FILTER = ${filter.toString} WHERE USERCODE = ${usercode.string}".execute()

}
