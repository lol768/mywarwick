package services.dao

import helpers.OneStartAppPerSuite
import models.{Platform, PushRegistration}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import warwick.sso.Usercode
import warwick.anorm.converters.ColumnConversions._

class PushRegistrationDaoTest extends PlaySpec with OneStartAppPerSuite {

  val pushRegistrationDao = app.injector.instanceOf[PushRegistrationDao]

  "PushNotificationDao" should {

    val usercode: Usercode = Usercode("cusjau")
    val now = DateTime.now
    val token = "IamToken"

    "retrieve registrations for user" in transaction { implicit c =>

      anorm.SQL("INSERT INTO push_registration VALUES ({usercode}, 'g', {token}, {now}, {now})")
        .on(
          'now -> now,
          'token -> token,
          'usercode -> usercode.string
        ).execute()

      pushRegistrationDao.getPushRegistrationsForUser(usercode) mustBe Seq(PushRegistration("cusjau", Platform("g"), token, now, now))
    }

    "retrieve all push registrations by token" in transaction { implicit c =>

      anorm.SQL("INSERT INTO push_registration VALUES ({usercode}, 'a', {token}, {now}, {now})")
        .on(
          'now -> now,
          'token -> token,
          'usercode -> usercode.string
        ).execute()

      pushRegistrationDao.getPushRegistrationByToken(token) mustBe PushRegistration("cusjau", Platform("a"), token, now, now)
    }

    "remove registration" in transaction { implicit c =>
      pushRegistrationDao.saveRegistration(usercode, Platform("a"), token)
      pushRegistrationDao.removeRegistration(token)

      pushRegistrationDao.getPushRegistrationsForUser(usercode) mustBe Seq()
    }
  }
}
