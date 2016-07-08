package services.dao

import helpers.OneStartAppPerSuite
import models.news.Audience
import models.news.Audience._
import org.scalatestplus.play.PlaySpec
import warwick.sso.GroupName

class AudienceDaoTest extends PlaySpec with OneStartAppPerSuite {

  val audienceDao = get[AudienceDaoImpl]

  "AudienceDao" should {

    "save Staff Component" in {
      val audience = Audience(Seq(Staff))
      val saved = audienceDao.audienceToComponents(audience)

      saved mustBe Seq(AudienceComponentSave("Staff", None, None))
    }

    "save CH Staff and CH Student Components" in {
      val audience = Audience(Seq(DepartmentAudience("ch", Seq(Staff, UndergradStudents))))
      val saved = audienceDao.audienceToComponents(audience)

      saved mustBe Seq(
        AudienceComponentSave("Staff", None, Some("ch")),
        AudienceComponentSave("UndergradStudents", None, Some("ch"))
      )
    }

    "save Module Component" in {
      val audience = Audience(Seq(ModuleAudience("music")))
      val saved = audienceDao.audienceToComponents(audience)

      saved mustBe Seq(AudienceComponentSave("Module", Some("music"), None))
    }

    "save Webgroup Component" in {
      val audience = Audience(Seq(WebgroupAudience(GroupName("in-music"))))
      val saved = audienceDao.audienceToComponents(audience)

      saved mustBe Seq(AudienceComponentSave("Webgroup", Some("in-music"), None))
    }

    "group mixed Components into Audience" in {
      val components = Seq(
        AudienceComponentSave("Staff", None, Some("ch")),
        AudienceComponentSave("UndergradStudents", None, Some("ch")),
        AudienceComponentSave("Module", Some("music"), None),
        AudienceComponentSave("Webgroup", Some("in-elab"), None),
        AudienceComponentSave("Module", Some("history"), None)
      )

      val audience = audienceDao.audienceFromComponents(components)

      audience mustBe Audience(Seq(
        DepartmentAudience("ch", Seq(Staff, UndergradStudents)),
        ModuleAudience("music"),
        WebgroupAudience(GroupName("in-elab")),
        ModuleAudience("history")
      ))
    }

    "group cross-DepartmentAudience Components into Audience" in {
      val components = Seq(
        AudienceComponentSave("Staff", None, Some("ch")),
        AudienceComponentSave("ResearchPostgrads", None, Some("fr")),
        AudienceComponentSave("Staff", None, Some("ec")),
        AudienceComponentSave("UndergradStudents", None, Some("ch"))
      )

      val audience = audienceDao.audienceFromComponents(components)

      audience mustBe Audience(Seq(
        DepartmentAudience("fr", Seq(ResearchPostgrads)),
        DepartmentAudience("ch", Seq(Staff, UndergradStudents)),
        DepartmentAudience("ec", Seq(Staff))
      ))
    }

    "group global Staff and PG Components into audience" in {
      val components = Seq(
        AudienceComponentSave("Staff", None, None),
        AudienceComponentSave("TaughtPostgrads", None, None),
        AudienceComponentSave("ResearchPostgrads", None, None)
      )

      val audience = audienceDao.audienceFromComponents(components)

      audience mustBe Audience(Seq(
        Staff,
        TaughtPostgrads,
        ResearchPostgrads
      ))
    }

    "read Components from db into Audience" in transaction {implicit c =>
      val audience = Audience(Seq(Staff))
      val id = audienceDao.saveAudience(audience)
      val audienceRead = audienceDao.getAudience(id)

      audienceRead mustBe audience
    }
  }
}
