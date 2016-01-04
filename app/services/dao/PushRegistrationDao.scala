package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.{Platform, PushRegistration}
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[PushRegistrationDaoImpl])
trait PushRegistrationDao {

  def getPushRegistrationsForUser(usercode: Usercode)(implicit c: Connection): Seq[PushRegistration]

  def saveSubscription(usercode: Usercode, platform: Platform, token: String)(implicit c: Connection): Boolean
  def registrationExists(token: String)(implicit c: Connection): Boolean

}

class PushRegistrationDaoImpl extends PushRegistrationDao {

  val pushRegistrationParser: RowParser[PushRegistration] =
    get[String]("USERCODE") ~
      get[String]("PLATFORM") ~
      get[String]("TOKEN") ~
      get[DateTime]("CREATED_AT") map {
      case usercode ~ platform ~ token ~ createdAt =>
        PushRegistration(usercode, Platform(platform), token, createdAt)
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

  override def saveSubscription(usercode: Usercode, platform: Platform, token: String)(implicit c: Connection): Boolean = {
    if (!registrationExists(token)) {
      SQL("INSERT INTO PUSH_REGISTRATION (TOKEN, usercode, platform, CREATED_AT) VALUES ({token}, {usercode}, {platform}, {now})")
        .on(
          'token -> token,
          'usercode -> usercode.string,
          'platform -> platform.dbValue,
          'now -> DateTime.now
        )
        .execute()
    } else {
      true
    }
  }

}
