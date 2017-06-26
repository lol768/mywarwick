package services.dao

import java.sql.Connection

import anorm.SqlParser.{get, scalar}
import anorm._
import com.google.inject.ImplementedBy
import warwick.sso.Usercode
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[EmailNotificationsPrefDaoImpl])
trait EmailNotificationsPrefDao {

  def getUserPreference(usercode: Usercode)(implicit c: Connection): Boolean

  def setUserPreference(usercode: Usercode, wantsEmail: Boolean)(implicit c: Connection): Unit

}

class EmailNotificationsPrefDaoImpl extends EmailNotificationsPrefDao {

  private val componentParser = {
    get[Boolean]("WANTS_EMAIL")
  }

  override def getUserPreference(usercode: Usercode)(implicit c: Connection): Boolean = {
    val result =
      SQL"""SELECT WANTS_EMAIL FROM USER_EMAIL_NOTIFICATIONS_PREF WHERE USERCODE = ${usercode.string}"""".as(scalar[Boolean].singleOpt)

    result.getOrElse(true)
  }

  private def _hasPreferenceRow(usercode: Usercode)(implicit c: Connection): Boolean = {
    val result =
      SQL"""SELECT COUNT(WANTS_EMAIL) FROM USER_EMAIL_NOTIFICATIONS_PREF WHERE USERCODE = ${usercode.string}"""".as(scalar[Int].single)
    result > 0
  }

  override def setUserPreference(usercode: Usercode, wantsEmail: Boolean)(implicit c: Connection): Unit = {
    if (_hasPreferenceRow(usercode)) {
      SQL"""UPDATE USER_EMAIL_NOTIFICATIONS_PREF SET WANTS_EMAIL = $wantsEmail WHERE USERCODE = ${usercode.string}"""".execute()
    } else {
      SQL"""INSERT INTO USER_EMAIL_NOTIFICATIONS_PREF VALUES (${usercode.string}, $wantsEmail)"""".execute()
    }
  }
}
