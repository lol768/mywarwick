package controllers.publish

import helpers.BaseSpec
import models.Audience.{UsercodesAudience, WebGroupAudience}
import org.mockito.Matchers
import org.mockito.Mockito._
import warwick.sso.{GroupName, Usercode}

class AudienceInfoHelperTest extends BaseSpec {

  "AudienceInfoHelperTest" should {

    "postProcessGroupedResolvedAudience should be happy with empty map" in {
      val result = AudienceInfoHelper.postProcessGroupedResolvedAudience(Map())
      result mustBe GroupedResolvedAudience(Set(),Map())
    }

    "postProcessGroupedResolvedAudience should correctly make baseaudience when the same user is in different audience component" in {
      val usercodeAudience = UsercodesAudience(Set(
        Usercode("123"),
        Usercode("456"),
        Usercode("789")
      ))
      val webgroupAudience = WebGroupAudience(GroupName("test-group"))

      val result: GroupedResolvedAudience = AudienceInfoHelper.postProcessGroupedResolvedAudience(Map(
        usercodeAudience -> usercodeAudience.usercodes,
        webgroupAudience -> Set(Usercode("123"))
      ))
      result.baseAudience.size mustBe 3
      result.groupedUsercodes.size mustBe 2
    }

    "postProcessGroupedResolvedAudience should remove duplicate user from each audience component" in {
      val usercodeAudience = UsercodesAudience(Set(
        Usercode("123"),
        Usercode("123"),
        Usercode("456"),
        Usercode("456"),
        Usercode("789")
      ))

      val webgroupAudience = WebGroupAudience(GroupName("test-group"))

      val result: GroupedResolvedAudience = AudienceInfoHelper.postProcessGroupedResolvedAudience(Map(
        usercodeAudience -> usercodeAudience.usercodes,
        webgroupAudience -> Set(Usercode("123"))
      ))
      result.baseAudience.size mustBe 3
      result.groupedUsercodes.size mustBe 2
      result.groupedUsercodes.toSeq.head._2.size mustBe 3
      result.groupedUsercodes.toSeq(1)._2.size mustBe 1
    }

  }
}
