package services.dao

import java.sql.{Connection, SQLIntegrityConstraintViolationException}

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.{Platform, PushRegistration}
import org.joda.time.DateTime
import system.Logging
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[PushRegistrationDaoImpl])
trait PushRegistrationDao {
  def getPushRegistrationsForUser(usercode: Usercode)(implicit c: Connection): Seq[PushRegistration]

  def getPushRegistrationByToken(token: String)(implicit c: Connection): PushRegistration

  def updateLastFetched(token: String)(implicit c: Connection): Unit

  def saveRegistration(usercode: Usercode, platform: Platform, token: String, deviceString: String)(implicit c: Connection): Boolean

  def registrationExists(token: String)(implicit c: Connection): Boolean

  def removeRegistration(token: String)(implicit c: Connection): Boolean

  def removeRegistrationIfNotRegisteredSince(token: String, date: DateTime)(implicit c: Connection): Boolean

}

class PushRegistrationDaoImpl extends PushRegistrationDao with Logging {

  val pushRegistrationParser: RowParser[PushRegistration] =
    get[String]("USERCODE") ~
      get[String]("PLATFORM") ~
      get[String]("TOKEN") ~
      get[DateTime]("CREATED_AT") ~
      get[DateTime]("LAST_FETCHED_AT") ~
      get[Option[DateTime]]("UPDATED_AT") ~
      get[Option[String]]("DEVICE_STRING") map {
      case usercode ~ platform ~ token ~ createdAt ~ lastFetchedAt ~ updatedAt ~ deviceString =>
        PushRegistration(usercode, Platform(platform), token, createdAt, lastFetchedAt, updatedAt, deviceString)
    }

  override def updateLastFetched(token: String)(implicit c: Connection): Unit = {
    SQL("UPDATE push_registration SET last_fetched_at = {now} WHERE token = {token}")
      .on(
        'now -> DateTime.now,
        'token -> token
      ).execute()
  }

  override def getPushRegistrationByToken(token: String)(implicit c: Connection): PushRegistration = {
    SQL("SELECT * FROM push_registration WHERE token = {token}")
      .on(
        'token -> token
      ).as(pushRegistrationParser.single)
  }

  override def getPushRegistrationsForUser(usercode: Usercode)(implicit c: Connection): Seq[PushRegistration] =
    SQL("SELECT * FROM PUSH_REGISTRATION WHERE usercode = {usercode}")
      .on(
        'usercode -> usercode.string
      )
      .as(pushRegistrationParser.*)

  override def registrationExists(token: String)(implicit c: Connection): Boolean = {
    SQL("SELECT COUNT(*) FROM PUSH_REGISTRATION WHERE token = {token}")
      .on(
        'token -> token
      ).as(scalar[Int].single) > 0
  }

  override def saveRegistration(usercode: Usercode, platform: Platform, token: String, deviceString: String)(implicit c: Connection): Boolean = {
    try {
      SQL("INSERT INTO PUSH_REGISTRATION (TOKEN, usercode, platform, CREATED_AT, LAST_FETCHED_AT, UPDATED_AT, DEVICE_STRING) VALUES ({token}, {usercode}, {platform}, {now}, {now}, {now}, {deviceString})")
        .on(
          'token -> token,
          'usercode -> usercode.string,
          'platform -> platform.dbValue,
          'now -> DateTime.now,
          'deviceString -> deviceString
        )
        .execute()
      true
    } catch {
      case _: SQLIntegrityConstraintViolationException =>
        // Token is already registered.  Update the registration to make sure it's for this user
        updateUsercodeForToken(token, usercode, deviceString)
        true
      case e: Exception =>
        logger.error("Exception when saving push registration", e)
        false
    }
  }

  private def updateUsercodeForToken(token: String, usercode: Usercode, deviceString: String)(implicit c: Connection): Boolean = {
    SQL("UPDATE PUSH_REGISTRATION SET USERCODE = {usercode}, UPDATED_AT = {now}, DEVICE_STRING = {deviceString} WHERE TOKEN = {token}")
      .on(
        'token -> token,
        'usercode -> usercode.string,
        'now -> DateTime.now,
        'deviceString -> deviceString
      )
      .executeUpdate() == 1
  }

  override def removeRegistration(token: String)(implicit c: Connection): Boolean = {
    SQL("DELETE FROM push_registration WHERE token = {token}")
      .on(
        'token -> token
      ).execute()
  }

  override def removeRegistrationIfNotRegisteredSince(token: String, date: DateTime)(implicit c: Connection): Boolean = {
    SQL("DELETE FROM PUSH_REGISTRATION WHERE TOKEN = {token} AND CREATED_AT < {date}")
      .on(
        'token -> token,
        'date -> date
      )
      .execute()
  }
}
