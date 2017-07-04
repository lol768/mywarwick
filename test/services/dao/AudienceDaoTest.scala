package services.dao

import helpers.OneStartAppPerSuite
import models.Audience
import models.Audience._
import helpers.BaseSpec
import warwick.sso.{GroupName, Usercode}

class AudienceDaoTest extends BaseSpec with OneStartAppPerSuite {

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

    "save WebGroup Component" in {
      val audience = Audience(Seq(WebGroupAudience(GroupName("in-music"))))
      val saved = audienceDao.audienceToComponents(audience)

      saved mustBe Seq(AudienceComponentSave("WebGroup", Some("in-music"), None))
    }

    "save Usercodes component" in {
      val audience = Audience.usercodes(Seq(Usercode("a"), Usercode("b")))
      val saved = audienceDao.audienceToComponents(audience)

      saved mustBe Seq(AudienceComponentSave("Usercode", Some("a"), None), AudienceComponentSave("Usercode", Some("b"), None))
    }

    "save Location opt-in component" in {
      val audience = Audience(Seq(Audience.LocationOptIn.CentralCampusResidences))
      val saved = audienceDao.audienceToComponents(audience)

      saved mustBe Seq(AudienceComponentSave("OptIn:Location", Some("CentralCampusResidences"), None))
    }

    "reconstitute usercodes audience" in {
      val components = Seq(
        AudienceComponentSave("Usercode", Some("a"), None),
        AudienceComponentSave("Usercode", Some("b"), None)
      )

      val audience = audienceDao.audienceFromComponents(components)

      audience mustBe Audience(Seq(
        Audience.UsercodeAudience(Usercode("a")),
        Audience.UsercodeAudience(Usercode("b"))
      ))
    }

    "group mixed Components into Audience" in {
      val components = Seq(
        AudienceComponentSave("Staff", None, Some("ch")),
        AudienceComponentSave("UndergradStudents", None, Some("ch")),
        AudienceComponentSave("Module", Some("music"), None),
        AudienceComponentSave("WebGroup", Some("in-elab"), None),
        AudienceComponentSave("Module", Some("history"), None)
      )

      val audience = audienceDao.audienceFromComponents(components)

      audience must not be 'public
      audience.components must contain only(
        DepartmentAudience("ch", Seq(Staff, UndergradStudents)),
        ModuleAudience("music"),
        WebGroupAudience(GroupName("in-elab")),
        ModuleAudience("history")
      )
    }

    "group cross-DepartmentAudience Components into Audience" in {
      val components = Seq(
        AudienceComponentSave("Staff", None, Some("ch")),
        AudienceComponentSave("ResearchPostgrads", None, Some("fr")),
        AudienceComponentSave("Staff", None, Some("ec")),
        AudienceComponentSave("UndergradStudents", None, Some("ch"))
      )

      val audience = audienceDao.audienceFromComponents(components)

      audience must not be 'public
      audience.components must contain only(
        DepartmentAudience("fr", Seq(ResearchPostgrads)),
        DepartmentAudience("ch", Seq(Staff, UndergradStudents)),
        DepartmentAudience("ec", Seq(Staff))
      )
    }

    "group global Staff and PG Components into audience" in {
      val components = Seq(
        AudienceComponentSave("Staff", None, None),
        AudienceComponentSave("TaughtPostgrads", None, None),
        AudienceComponentSave("ResearchPostgrads", None, None)
      )

      val audience = audienceDao.audienceFromComponents(components)

      audience must not be 'public
      audience.components must contain only(
        Staff,
        TaughtPostgrads,
        ResearchPostgrads
      )
    }

    "read Components from db into Audience" in transaction {implicit c =>
      val audience = Audience(Seq(Staff))
      val id = audienceDao.saveAudience(audience)
      val audienceRead = audienceDao.getAudience(id)

      audienceRead mustBe audience
    }
  }
}
