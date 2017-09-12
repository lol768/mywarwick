package services.elasticsearch

import helpers.BaseSpec
import models.Audience
import models.Audience._
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
    "serialise audience component correctly for UsercodeAudience" in new Scope {

      val audience = Audience.usercode(Usercode("usercode123"))
      when(audienceService.getAudience((Matchers.any()))).thenReturn(audience)
      val result = ActivityDocument.serialiseAudienceComponents(Some("1"), audienceService)
      result must be(Seq("UsercodeAudience")
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
