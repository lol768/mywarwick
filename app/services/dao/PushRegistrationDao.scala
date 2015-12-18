package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.PushRegistration
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[PushRegistrationDaoImpl])
trait PushRegistrationDao {

  def getPushRegistrationsForUser(usercode: String)(implicit c: Connection): Seq[PushRegistration]

}

class PushRegistrationDaoImpl extends PushRegistrationDao {

  val pushRegistrationParser: RowParser[PushRegistration] =
    get[String]("USERCODE") ~
      get[String]("PLATFORM") ~
      get[String]("TOKEN") ~
      get[DateTime]("CREATED_AT") map {
      case usercode ~ platform ~ token ~ createdAt =>
        PushRegistration(usercode, platform, token, createdAt)
    }

  override def getPushRegistrationsForUser(usercode: String)(implicit c: Connection): Seq[PushRegistration] =
    SQL("SELECT * FROM PUSH_REGISTRATION WHERE usercode = {usercode}")
      .on(
        'usercode -> usercode
      )
      .as(pushRegistrationParser.*)

}
