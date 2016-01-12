package services.dao

import helpers.OneStartAppPerSuite
import models.{Platform, PushRegistration}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import warwick.sso.Usercode
import warwick.anorm.converters.ColumnConversions._

class PushRegistrationDaoTest extends PlaySpec with OneStartAppPerSuite {

  val dao = app.injector.instanceOf[PushRegistrationDao]

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

      dao.getPushRegistrationsForUser(usercode) mustBe Seq(PushRegistration("cusjau", Platform("g"), token, now, now))
    }

    "retrieve all push registrations by token" in transaction { implicit c =>

      anorm.SQL("INSERT INTO push_registration VALUES ({usercode}, 'a', {token}, {now}, {now})")
        .on(
          'now -> now,
          'token -> token,
          'usercode -> usercode.string
        ).execute()

      dao.getPushRegistrationByToken(token) mustBe PushRegistration("cusjau", Platform("a"), token, now, now)
    }

    "remove registration" in transaction { implicit c =>
      dao.saveRegistration(usercode, Platform("a"), token)
      dao.removeRegistration(token)

      dao.getPushRegistrationsForUser(usercode) mustBe Seq()
    }

    "remove registration if older than x" in transaction { implicit c =>
      dao.saveRegistration(usercode, Platform("a"), token)

      dao.removeRegistrationIfNotRegisteredSince(token, DateTime.now.minusHours(1))
      dao.getPushRegistrationsForUser(usercode).length mustBe 1

      dao.removeRegistrationIfNotRegisteredSince(token, DateTime.now.plusHours(1))
      dao.getPushRegistrationsForUser(usercode) mustBe Seq()
    }
  }
}
