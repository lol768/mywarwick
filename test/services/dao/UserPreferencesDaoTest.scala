package services.dao

import anorm.SqlParser._
import anorm._
import helpers.{BaseSpec, OneStartAppPerSuite, TestApplications}
import models.FeaturePreferences
import org.joda.time.DateTime
import play.api.Application
import warwick.sso.Usercode

class UserPreferencesDaoTest extends BaseSpec with OneStartAppPerSuite {

  override lazy val app: Application = TestApplications.fullNoRoutes()

  private val dao = get[UserPreferencesDao]

  "UserPreferencesDao" should {

    val custard = Usercode("custard")

    "check if user preferences exist" in transaction { implicit c =>
      dao.exists(custard) mustBe false

      SQL"INSERT INTO USER_PREFERENCE (USERCODE, CREATED_AT) VALUES ('custard', SYSDATE)"
        .execute()

      dao.exists(custard) mustBe true
    }

    "create user preferences" in transaction { implicit c =>
      SQL"SELECT USERCODE FROM USER_PREFERENCE WHERE USERCODE = 'custard'"
        .executeQuery()
        .as(scalar[String].singleOpt) mustBe empty

      dao.save(custard)

      SQL"SELECT USERCODE FROM USER_PREFERENCE WHERE USERCODE = 'custard'"
        .executeQuery()
        .as(scalar[String].singleOpt) must contain("custard")
    }

    "get colour schemes" in transaction { implicit c =>
      dao.save(custard)
      dao.getColourSchemePreference(custard).highContrast mustBe false
    }

    "update EAP" in transaction { implicit c =>
      val until = DateTime.now.plusHours(1)
      dao.setFeaturePreferences(custard, FeaturePreferences(Some(until)))
      val enabled = dao.getFeaturePreferences(custard)
      enabled.eap mustBe true
      enabled.eapUntil.isDefined mustBe true
      enabled.eapUntil.get.isEqual(until) mustBe true

      dao.setFeaturePreferences(custard, FeaturePreferences(None))
      val disabled = dao.getFeaturePreferences(custard)
      disabled.eap mustBe false
      disabled.eapUntil.isDefined mustBe false
    }

  }

}
