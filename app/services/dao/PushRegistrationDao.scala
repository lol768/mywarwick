package services.dao

import java.sql.Connection

import actors.WebsocketActor.Notification
import anorm.SqlParser._
import anorm._
import com.google.inject.{Inject, ImplementedBy}
import models.{Activity, Platform, PushRegistration}
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[PushRegistrationDaoImpl])
trait PushRegistrationDao {

  def getPushRegistrationsForUser(usercode: Usercode)(implicit c: Connection): Seq[PushRegistration]

  def getPushRegistrationByToken(token: String)(implicit c: Connection): PushRegistration

  def saveRegistration(usercode: Usercode, platform: Platform, token: String)(implicit c: Connection): Boolean

  def registrationExists(token: String)(implicit c: Connection): Boolean

}

class PushRegistrationDaoImpl @Inject()(
  activityDao: ActivityDao
) extends PushRegistrationDao {

  val pushRegistrationParser: RowParser[PushRegistration] =
    get[String]("USERCODE") ~
      get[String]("PLATFORM") ~
      get[String]("TOKEN") ~
      get[DateTime]("CREATED_AT") ~
      get[DateTime]("LAST_FETCHED_AT") map {
      case usercode ~ platform ~ token ~ createdAt ~ lastFetchedAt =>
        PushRegistration(usercode, Platform(platform), token, createdAt, lastFetchedAt)
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

  override def saveRegistration(usercode: Usercode, platform: Platform, token: String)(implicit c: Connection): Boolean = {
    try {
      if (!registrationExists(token)) {
        SQL("INSERT INTO PUSH_REGISTRATION (TOKEN, usercode, platform, CREATED_AT, LAST_FETCHED_AT) VALUES ({token}, {usercode}, {platform}, {now}, {now})")
          .on(
            'token -> token,
            'usercode -> usercode.string,
            'platform -> platform.dbValue,
            'now -> DateTime.now
          )
          .execute()
      }
      true
    }
    catch {
      case e: Exception => false
    }
  }
}
