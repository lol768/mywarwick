package services

import helpers.TestObjectFactory._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import warwick.sso._

import scala.util.Success

class ActivityTargetServiceTest extends PlaySpec with MockitoSugar with Results {

  "ActivityTargetService" should {

    val userLookupService = mock[UserLookupService]
    val groupService = mock[GroupService]

    val service = new ActivityTargetServiceImpl(userLookupService, groupService)

    "enumerate the members of a group and deduplicate" in {

      when(groupService.getWebGroup(GroupName("in-elab"))).thenReturn(Success(Some(makeGroup())))

      when(userLookupService.getUsers(Seq(Usercode("a")))).thenReturn(
        Success(Map(
          Usercode("a") -> makeFoundUser()
        ))
      )

      service.getRecipients(Seq(Usercode("a")), Seq(GroupName("in-elab"))) mustBe Set(Usercode("a"), Usercode("b"))

    }

    "discard invalid usercodes" in {

      when(userLookupService.getUsers(Seq(Usercode("csumbo"), Usercode("invalid")))).thenReturn(
        Success(Map(
          Usercode("csumbo") -> makeFoundUser(),
          Usercode("invalid") -> makeNotFoundUser()
        ))
      )

      service.getRecipients(Seq(Usercode("csumbo"), Usercode("invalid")), Seq.empty) mustBe Set(Usercode("csumbo"))

    }

  }

}
