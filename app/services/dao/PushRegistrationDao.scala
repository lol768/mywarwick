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

}
