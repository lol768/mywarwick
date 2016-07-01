package services.dao

import anorm._
import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec

class ActivityTypeDaoTest extends PlaySpec with OneStartAppPerSuite {

  val dao = get[ActivityTypeDao]

  "ActivityTypeDao" should {

    "check for existence of an ACTIVITY_TYPE" in transaction { implicit c =>
      dao.isValidActivityType("something") mustBe false

      SQL"INSERT INTO ACTIVITY_TYPE (NAME, DISPLAY_NAME) VALUES ('something', 'Something')"
        .execute()

      dao.isValidActivityType("something") mustBe true
    }

    "check for existence of an ACTIVITY_TAG_TYPE" in transaction { implicit c =>
      dao.isValidActivityTagName("module") mustBe false

      SQL"INSERT INTO ACTIVITY_TAG_TYPE (NAME, DISPLAY_NAME) VALUES ('module', 'Module')"
        .execute()

      dao.isValidActivityTagName("module") mustBe true
    }

    "return a VALUE_VALIDATION_REGEX if one exists" in transaction { implicit c =>
      dao.getValueValidationRegex("module") mustBe empty

      SQL"INSERT INTO ACTIVITY_TAG_TYPE (NAME, DISPLAY_NAME) VALUES ('module', 'Module')"
        .execute()

      dao.getValueValidationRegex("module") mustBe empty

      SQL"UPDATE ACTIVITY_TAG_TYPE SET VALUE_VALIDATION_REGEX = 'regex' WHERE NAME = 'module'"
        .execute()

      dao.getValueValidationRegex("module") mustBe Some("regex")
    }

  }

}
