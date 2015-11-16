package services

import java.util
import java.util.Date

import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import uk.ac.warwick.userlookup
import uk.ac.warwick.userlookup.webgroups.{GroupNotFoundException, GroupServiceException}
import warwick.sso.{Department, Usercode}

import scala.util.{Failure, Success}

class GroupServiceTest extends PlaySpec with MockitoSugar with Results {

  "GroupService" should {

    val underlyingGroupService = mock[userlookup.GroupService]

    val underlyingGroupServiceFactory = new UnderlyingGroupServiceFactory {
      override def groupService: userlookup.GroupService = underlyingGroupService
    }

    val service = new GroupServiceImpl(underlyingGroupServiceFactory)

    "return Failure(e) on GroupServiceException" in {

      val exception = new GroupServiceException("sadness")

      reset(underlyingGroupService)
      when(underlyingGroupService.getGroupByName("in-elab")).thenThrow(exception)

      service.getWebGroup(GroupName("in-elab")) mustBe Failure(exception)

    }

    "return Success(None) on GroupNotFoundException" in {

      reset(underlyingGroupService)
      when(underlyingGroupService.getGroupByName("in-elab")).thenThrow(new GroupNotFoundException("in-elab"))

      service.getWebGroup(GroupName("in-elab")) mustBe Success(None)

    }

    "return Success(Some(group)) on success" in {

      val date = new Date
      val group = new userlookup.GroupImpl
      group.setName("in-elab")
      group.setTitle("ITS web team")
      group.setUserCodes(util.Arrays.asList("a", "b", "c"))
      group.setOwners(util.Arrays.asList("a"))
      group.setType("Arbitrary")
      group.setDepartment("Information Technology Services")
      group.setDepartmentCode("in")
      group.setLastUpdatedDate(date)

      reset(underlyingGroupService)
      when(underlyingGroupService.getGroupByName("in-elab")).thenReturn(group)

      service.getWebGroup(GroupName("in-elab")) mustBe Success(Some(Group(
        GroupName("in-elab"),
        Some("ITS web team"),
        Seq(Usercode("a"), Usercode("b"), Usercode("c")),
        Seq(Usercode("a")),
        "Arbitrary",
        Department(None, Some("Information Technology Services"), Some("in")),
        new DateTime(date)
      )))

    }
  }

}
