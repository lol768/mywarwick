package services

import models.Audience
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import helpers.BaseSpec
import services.dao.AudienceDao
import warwick.sso._

import scala.util.Success

class AudienceServiceTest extends BaseSpec with MockitoSugar {
  trait Ctx {
    val webgroups = mock[GroupService]
    val audienceDao = mock[AudienceDao]
    val service = new AudienceServiceImpl(webgroups, audienceDao, new MockDatabase)


    def webgroupsIsEmpty: Unit = {
      when(webgroups.getWebGroup(any())).thenReturn(Success(None)) // this webgroups is empty
      when(webgroups.getGroupsForQuery(any())).thenReturn(Success(Nil))
    }

    def newGroup(name: String, users: Seq[String], `type`:String="Arbitrary") =
      Group(GroupName(name), None, users.map(Usercode), Nil, `type`, null, null, restricted = false)

    // Welcome to Barry's World
    def webgroupsAllContainBarry: Unit = {
      val barryGroup = newGroup("in-barry-world", Seq("cuddz"))
      when(webgroups.getWebGroup(any())).thenReturn(Success(Some(barryGroup)))
      when(webgroups.getGroupsForQuery(any())).thenReturn(Success(Seq(barryGroup)))
    }
  }

  import Audience._

  "AudienceService" should {

    "return an empty list for an empty audience" in new Ctx {
      service.resolve(Audience()).get must be (Nil)
    }

    "search for research postgrads" in new Ctx {
      webgroupsIsEmpty
      service.resolve(Audience(Seq(ResearchPostgrads))).get
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-research-ft"))
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-research-pt"))
      verifyNoMoreInteractions(webgroups)
    }

    "search for taught postgrads" in new Ctx {
      webgroupsIsEmpty
      service.resolve(Audience(Seq(TaughtPostgrads))).get
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-taught-ft"))
      verify(webgroups).getWebGroup(GroupName("all-studenttype-postgraduate-taught-pt"))
      verifyNoMoreInteractions(webgroups)
    }

    "search for combination of departmental subsets" in new Ctx {
      webgroupsIsEmpty
      service.resolve(Audience(Seq(
        WebgroupAudience(GroupName("in-winners")),
        WebgroupAudience(GroupName("in-losers")),
        ModuleAudience("CS102"),
        DepartmentAudience("CH", Seq(UndergradStudents)),
        DepartmentAudience("PH", Seq(UndergradStudents, TeachingStaff))
      ))).get
      verify(webgroups).getWebGroup(GroupName("in-winners"))
      verify(webgroups).getWebGroup(GroupName("in-losers"))
      verify(webgroups).getGroupsForQuery("-cs102")
      verify(webgroups).getWebGroup(GroupName("ch-studenttype-undergraduate-full-time"))
      verify(webgroups).getWebGroup(GroupName("ch-studenttype-undergraduate-part-time"))
      verify(webgroups).getWebGroup(GroupName("ph-studenttype-undergraduate-full-time"))
      verify(webgroups).getWebGroup(GroupName("ph-studenttype-undergraduate-part-time"))
      verify(webgroups).getWebGroup(GroupName("ph-teaching"))
      verifyNoMoreInteractions(webgroups)
    }

    "match an uppercase module group" in new Ctx {
      val unrelatedGroup = newGroup("in-cs102-eggbox", Seq("ada","bev"), `type`="Module")
      val moduleGroup = newGroup("in-cs102", Seq("cuuaaa","cuuaab"), `type`="Module")
      when(webgroups.getGroupsForQuery("-cs102")).thenReturn(Success(Seq(unrelatedGroup, moduleGroup)))

      val users = service.resolve(Audience(Seq( ModuleAudience("CS102") ))).get
      users.map(_.string) must be (Seq("cuuaaa","cuuaab"))
    }

    "deduplicate usercodes" in new Ctx {
      webgroupsAllContainBarry
      val users = service.resolve(Audience(Seq(
        ResearchPostgrads, TaughtPostgrads, UndergradStudents
      ))).get

      users must be (Seq(Usercode("cuddz")))
    }

  }
}
