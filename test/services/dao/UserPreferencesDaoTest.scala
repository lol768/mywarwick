package services.dao

import anorm.SqlParser._
import anorm._
import helpers.{BaseSpec, OneStartAppPerSuite, TestApplications}
import warwick.sso.Usercode

class UserPreferencesDaoTest extends BaseSpec with OneStartAppPerSuite {

  override lazy val app = TestApplications.fullNoRoutes()

  val dao = get[UserPreferencesDao]

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

  }

}
