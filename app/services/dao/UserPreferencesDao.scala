package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[UserPreferencesDaoImpl])
trait UserPreferencesDao {

  def exists(usercode: Usercode)(implicit c: Connection): Boolean

  def save(usercode: Usercode)(implicit c: Connection): Unit

  def countInitialisedUsers(usercodes: Seq[Usercode])(implicit c: Connection): Int

}

@Singleton
class UserPreferencesDaoImpl extends UserPreferencesDao {

  override def exists(usercode: Usercode)(implicit c: Connection) =
    SQL"SELECT CREATED_AT FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(scalar[DateTime].singleOpt)
      .nonEmpty

  override def save(usercode: Usercode)(implicit c: Connection) =
    SQL"INSERT INTO USER_PREFERENCE (USERCODE, CREATED_AT) VALUES (${usercode.string}, SYSDATE)"
      .execute()

  override def countInitialisedUsers(usercodes: Seq[Usercode])(implicit c: Connection) =
    usercodes.grouped(1000).map { groupedUsercodes =>
      SQL"SELECT COUNT(*) FROM USER_PREFERENCE WHERE USERCODE IN (${groupedUsercodes.map(_.string)})"
        .as(scalar[Int].single)
    }.sum

}
