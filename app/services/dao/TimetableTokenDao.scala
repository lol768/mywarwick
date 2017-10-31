package services.dao

import java.sql.Connection
import javax.inject.Singleton

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

case class TimetableToken(string: String)

@ImplementedBy(classOf[TimetableTokenDaoImpl])
trait TimetableTokenDao {
  def create(usercode: Usercode, token: String)(implicit c: Connection): TimetableToken

  def validate(token: String)(implicit c: Connection): Option[Usercode]
}

@Singleton
class TimetableTokenDaoImpl extends TimetableTokenDao {
  override def create(usercode: Usercode, token: String)(implicit c: Connection): TimetableToken = {
    SQL"INSERT INTO TIMETABLE_TOKEN (USERCODE, TOKEN) VALUES (${usercode.string}, $token)"
      .execute()

    TimetableToken(token)
  }

  override def validate(token: String)(implicit c: Connection): Option[Usercode] = {
    val now = DateTime.now()

    val result: Option[Usercode] = SQL"SELECT USERCODE FROM TIMETABLE_TOKEN WHERE TOKEN = $token"
      .executeQuery()
      .as(get[String]("USERCODE").map(Usercode).singleOpt)

    result.foreach { _ =>
      SQL"UPDATE TIMETABLE_TOKEN SET LAST_USED_AT = $now WHERE TOKEN = $token"
        .execute()
    }

    result
  }
}
