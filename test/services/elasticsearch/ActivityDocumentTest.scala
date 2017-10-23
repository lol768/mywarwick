package services.elasticsearch

import helpers.BaseSpec
import models.Audience
import models.Audience._
import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar
import services.AudienceService
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import warwick.sso.{GroupName, Usercode}


class ActivityDocumentTest extends BaseSpec with MockitoSugar {

  class Scope {
    val audienceService = mock[AudienceService]
  }


  "ActivityDocument" should {
    "build activity document from ES response" in {
      val map = Map[String, AnyRef](
        "activity_id" -> "xyz",
        "title" -> "Hello",
        "created_at" -> "2017-10-23T14:18:21+00:00",
        "created_by" -> "custard"
      )

      val doc = ActivityDocument.fromMap(map)

      doc.activity_id mustBe "xyz"
      doc.title mustBe "Hello"
      doc.created_at mustBe DateTime.parse("2017-10-23T14:18:21+00:00")
      doc.created_by mustBe "custard"
    }

    "handle null dates" in {
      val doc = ActivityDocument.fromMap(Map.empty)
      doc.published_at mustBe null
      doc.created_at mustBe null
    }

    "serialise audience component correctly for UsercodesAudience" in new Scope {
      val audience = Audience.usercodes(Seq(Usercode("usercode123"), Usercode("usercode456")))
      when(audienceService.getAudience((Matchers.any()))).thenReturn(audience)
      val result = ActivityDocument.serialiseAudienceComponents(Some("1"), audienceService)
      result must be(Seq("UsercodesAudience")
      )
    }


    "serialise audience component correctly for WebGroupAudience" in new Scope {
      val audience = Audience.webGroup(GroupName("123123"))
      when(audienceService.getAudience((Matchers.any()))).thenReturn(audience)
      val result = ActivityDocument.serialiseAudienceComponents(Some("1"), audienceService)
      result must be(Seq("WebGroupAudience:123123")
      )
    }


    "serialise audience component correctly for DepartmentAudience" in new Scope {
      val departmentAudience = DepartmentAudience("DC", Seq(
        Audience.Staff,
        Audience.TeachingStaff
      ))

      val audience = new Audience(Seq(departmentAudience))


      when(audienceService.getAudience((Matchers.any()))).thenReturn(audience)
      val result = ActivityDocument.serialiseAudienceComponents(Some("1"), audienceService)
      result must be(Seq("DepartmentAudience:DC:Staff", "DepartmentAudience:DC:TeachingStaff")
      )
    }

    "serialise audience component correctly for ModuleAudience" in new Scope {

      val audience = new Audience(Seq(ModuleAudience("scala-101")))
      when(audienceService.getAudience((Matchers.any()))).thenReturn(audience)
      val result = ActivityDocument.serialiseAudienceComponents(Some("1"), audienceService)
      result must be(Seq("ModuleAudience:scala-101")
      )
    }

  }

}
