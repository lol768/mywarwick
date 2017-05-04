package services.dao

import helpers.OneStartAppPerSuite
import models.{Platform, PushRegistration}
import org.joda.time.DateTime
import helpers.BaseSpec
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import warwick.sso.Usercode
import warwick.anorm.converters.ColumnConversions._

class PushRegistrationDaoTest extends BaseSpec with MockitoSugar with OneStartAppPerSuite {

  private val deviceStringMaxLength = 10

  val config = mock[Configuration]
  when(config.getInt(any[String])) thenReturn(Some(deviceStringMaxLength))
  val dao = new PushRegistrationDaoImpl(config)

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

    "return none when inserting a null device string" in transaction { implicit c =>

      anorm.SQL("INSERT INTO push_registration VALUES ({usercode}, 'a', {token}, {now}, {now}, {now}, null)")
        .on(
          'now -> now,
          'token -> token,
          'usercode -> usercode.string
        ).execute()

      dao.getPushRegistrationByToken(token) mustBe PushRegistration("cusjau", Platform("a"), token, now, now, Some(now), None)
    }

    "save a device string of None" in transaction { implicit c =>
      dao.saveRegistration(usercode, Platform("a"), token, None)
      val result = dao.getPushRegistrationsForUser(usercode)
      result.length mustBe 1
      result.head.deviceString mustBe None
    }

    "truncate a device string longer than the maximum length" in transaction { implicit c =>
        dao.saveRegistration(usercode, Platform("a"), token, Some("abcdefghijklmnopqrstuvwxyz"))
        val result = dao.getPushRegistrationsForUser(usercode)
        result.length mustBe 1
        result.head.deviceString mustBe Some("abcdefghij")
    }

    "remove registration" in transaction { implicit c =>
      dao.saveRegistration(usercode, Platform("a"), token, Some("Chrome 79"))
      dao.removeRegistration(token)

      dao.getPushRegistrationsForUser(usercode) mustBe Seq()
    }

    "remove registration if older than x" in transaction { implicit c =>
      dao.saveRegistration(usercode, Platform("a"), token, Some("Chrome 79"))

      dao.removeRegistrationIfNotRegisteredSince(token, DateTime.now.minusHours(1))
      dao.getPushRegistrationsForUser(usercode).length mustBe 1

      dao.removeRegistrationIfNotRegisteredSince(token, DateTime.now.plusHours(1))
      dao.getPushRegistrationsForUser(usercode) mustBe Seq()
    }
  }
}
