package services.dao

import java.sql.Connection

import anorm._
import anorm.SqlParser._
import com.google.inject.ImplementedBy
import models.messaging.DoNotDisturbPeriod
import warwick.sso.Usercode

@ImplementedBy(classOf[DoNotDisturbDaoImpl])
trait DoNotDisturbDao {
  def get(user: Usercode)(implicit c: Connection): Option[DoNotDisturbPeriod]

  def set(user: Usercode, doNotDisturbPeriod: DoNotDisturbPeriod)(implicit c: Connection): Unit

  def disable(user: Usercode)(implicit c: Connection): Int
}

class DoNotDisturbDaoImpl extends DoNotDisturbDao {
  override def get(user: Usercode)(implicit c: Connection): Option[DoNotDisturbPeriod] =
    SQL(s"SELECT start_hr, start_min, end_hr, end_min FROM do_not_disturb WHERE usercode={usercode}")
      .on('usercode -> user.string)
      .as(DoNotDisturbPeriod.rowParser.singleOpt)

  private def exists(user: Usercode)(implicit c: Connection): Boolean =
    SQL(s"SELECT count(*) FROM do_not_disturb WHERE usercode={usercode}")
      .on('usercode -> user.string)
      .as(scalar[Int].single) > 0

  override def set(user: Usercode, doNotDisturbPeriod: DoNotDisturbPeriod)(implicit c: Connection): Unit = {
    import doNotDisturbPeriod._
    if (exists(user)) {
      SQL(s"UPDATE do_not_disturb SET start_hr=${start.hr}, start_min=${start.min}, end_hr=${end.hr}, end_min=${end.min} WHERE usercode={usercode}")
        .on('usercode -> user.string)
        .executeUpdate()
    } else {
      SQL(s"INSERT INTO do_not_disturb (usercode, start_hr, start_min, end_hr, end_min) VALUES ({usercode}, ${start.hr}, ${start.min}, ${end.hr}, ${end.min})")
        .on('usercode -> user.string)
        .execute()
    }
  }

  override def disable(user: Usercode)(implicit c: Connection): Int =
    SQL(s"DELETE FROM do_not_disturb WHERE usercode={usercode}")
      .on('usercode -> user.string)
      .executeUpdate()
}
