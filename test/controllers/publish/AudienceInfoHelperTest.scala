package controllers.publish

import helpers.BaseSpec
import models.Audience.{LocationOptIn, UsercodesAudience, WebGroupAudience}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import warwick.sso.{GroupName, Usercode}

class AudienceInfoHelperTest extends BaseSpec {

  "AudienceInfoHelperTest" should {

    "postProcessGroupedResolvedAudience should be happy with empty map" in {
      val result = AudienceInfoHelper.postProcessGroupedResolvedAudience(Map())
      result mustBe GroupedResolvedAudience(Set(), Map())
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

    "postProcessGroupedResolvedAudience should work properly when supplied with location opt in audiences" in {
      val usercodeAudience = UsercodesAudience(Set(
        Usercode("123"),
        Usercode("123"),
        Usercode("456"),
        Usercode("456"),
        Usercode("789"),
        Usercode("Lan")
      ))

      val webgroupAudience = WebGroupAudience(GroupName("test-group"))
      val webgroupUsercodes = Set(Usercode("123"), Usercode("Kai"), Usercode("789"))

      val leamington = LocationOptIn.LeamingtonSpa
      val leamingtonUsers = Set(Usercode("Kai"), Usercode("Lan"))

      val oncampus = LocationOptIn.CentralCampusResidences
      val oncampusUsers = Set(Usercode("Lan"))

      val result: GroupedResolvedAudience = AudienceInfoHelper.postProcessGroupedResolvedAudience(Map(
        usercodeAudience -> usercodeAudience.usercodes,
        leamington -> leamingtonUsers,
        oncampus -> oncampusUsers,
        webgroupAudience -> Set(Usercode("123"))
      ))
      result.baseAudience.size mustBe 1
      result.groupedUsercodes.size mustBe 2
      result.groupedUsercodes.head._2.size mustBe 1
      result.groupedUsercodes.last._2.size mustBe 0
    }

  }
}
