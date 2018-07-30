package services.dao

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import helpers.{BaseSpec, OneStartAppPerSuite}
import models.messaging.DoNotDisturbPeriod
import warwick.sso.Usercode
import anorm._

class DoNotDisturbDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val doNotDisturbDao = get[DoNotDisturbDaoImpl]

  "DoNotDisturbDao" should {
    val sam: Usercode = Usercode("sam")

    "find get no dnd if not enabled" in transaction {implicit c =>
      doNotDisturbDao.get(sam) mustBe None
    }

    "insert dnd entry if user doesn't already have dnd enabled" in transaction { implicit c =>
      SQL"INSERT INTO USER_PREFERENCE (USERCODE, CREATED_AT) VALUES (${sam.string}, SYSDATE)".execute()

      val dnd: DoNotDisturbPeriod = DoNotDisturbPeriod(LocalTime.of(11, 0), LocalTime.of(13, 0))
      doNotDisturbDao.set(sam, dnd)

      doNotDisturbDao.get(sam) mustBe Some(dnd)
    }

    "update dnd entry if user already has dnd enabled" in transaction { implicit c =>
      SQL"INSERT INTO USER_PREFERENCE (USERCODE, CREATED_AT) VALUES (${sam.string}, SYSDATE)".execute()

      val initialStartTime: LocalTime = LocalTime.of(21, 0)
      val initialEndTime: LocalTime = LocalTime.of(7, 0)
      SQL"""INSERT INTO do_not_disturb (usercode, start_time, end_time) VALUES (${sam.string},
            ${initialStartTime.format(DoNotDisturbPeriod.dateTimeFormatter)}, ${initialEndTime.format(DoNotDisturbPeriod.dateTimeFormatter)})
        """.execute()

      doNotDisturbDao.get(sam).map(_.start) mustBe Some(initialStartTime)
      doNotDisturbDao.get(sam).map(_.end) mustBe Some(initialEndTime)

      val dnd = DoNotDisturbPeriod(LocalTime.of(7, 0), LocalTime.of(11,0))
      doNotDisturbDao.set(sam, dnd)

      doNotDisturbDao.get(sam) mustBe Some(dnd)
    }

    "remove dnd entries on disable" in transaction { implicit c =>
      SQL"INSERT INTO USER_PREFERENCE (USERCODE, CREATED_AT) VALUES (${sam.string}, SYSDATE)".execute()

      val dnd = DoNotDisturbPeriod(LocalTime.of(7, 0), LocalTime.of(11,0))
      doNotDisturbDao.set(sam, dnd)

      doNotDisturbDao.get(sam) mustBe Some(dnd)
      doNotDisturbDao.disable(sam)
      doNotDisturbDao.get(sam) mustBe None
    }
  }

}
