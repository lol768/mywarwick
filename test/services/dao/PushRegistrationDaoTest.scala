package services.dao

import helpers.OneStartAppPerSuite
import models.{Platform, PushRegistration}
import org.joda.time.DateTime
import helpers.BaseSpec
import warwick.sso.Usercode
import warwick.anorm.converters.ColumnConversions._

class PushRegistrationDaoTest extends BaseSpec with OneStartAppPerSuite {

  val dao = app.injector.instanceOf[PushRegistrationDao]

  "PushNotificationDao" should {

    val usercode: Usercode = Usercode("cusjau")
    val now = DateTime.now
    val token = "IamToken"

    "retrieve registrations for user" in transaction { implicit c =>

      anorm.SQL("INSERT INTO push_registration VALUES ({usercode}, 'g', {token}, {now}, {now}, {now}, 'Chrome 79')")
        .on(
          'now -> now,
          'token -> token,
          'usercode -> usercode.string
        ).execute()

      dao.getPushRegistrationsForUser(usercode) mustBe Seq(PushRegistration("cusjau", Platform("g"), token, now, now, Some(now), Some("Chrome 79")))
    }

    "retrieve all push registrations by token" in transaction { implicit c =>

      anorm.SQL("INSERT INTO push_registration VALUES ({usercode}, 'a', {token}, {now}, {now}, {now}, 'Chrome 79')")
        .on(
          'now -> now,
          'token -> token,
          'usercode -> usercode.string
        ).execute()

      dao.getPushRegistrationByToken(token) mustBe PushRegistration("cusjau", Platform("a"), token, now, now, Some(now), Some("Chrome 79"))
    }

    "remove registration" in transaction { implicit c =>
      dao.saveRegistration(usercode, Platform("a"), token, "Chrome 79")
      dao.removeRegistration(token)

      dao.getPushRegistrationsForUser(usercode) mustBe Seq()
    }

    "remove registration if older than x" in transaction { implicit c =>
      dao.saveRegistration(usercode, Platform("a"), token, "Chrome 79")

      dao.removeRegistrationIfNotRegisteredSince(token, DateTime.now.minusHours(1))
      dao.getPushRegistrationsForUser(usercode).length mustBe 1

      dao.removeRegistrationIfNotRegisteredSince(token, DateTime.now.plusHours(1))
      dao.getPushRegistrationsForUser(usercode) mustBe Seq()
    }
  }
}
