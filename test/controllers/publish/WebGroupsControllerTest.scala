package controllers.publish

import helpers.{BaseSpec, Fixtures}
import models.publishing.PermissionScope.{AllDepartments, Departments}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.PublisherService
import warwick.sso.GroupService

import scala.util.Success

class WebGroupsControllerTest extends BaseSpec with MockitoSugar with Results {

  private val groupService = mock[GroupService]
  private val publisherService = mock[PublisherService]

  "WebGroupsController" should {
    "return a list of matching WebGroups" in {
      val controller = new WebGroupsController(groupService, publisherService)

      when(publisherService.getPermissionScope("test")).thenReturn(AllDepartments)
      when(groupService.getGroupsForQuery("elab")).thenReturn(Success(Seq(Fixtures.user.makeGroup())))

      val result = controller.results("test", "elab")(FakeRequest())

      status(result) mustBe 200
      (contentAsJson(result) \ "success").as[Boolean] mustBe true
      (contentAsJson(result) \ "status").as[String] mustBe "ok"
      (contentAsJson(result) \ "groups" \\ "name").map(_.as[String]) mustBe Seq("in-elab")
      (contentAsJson(result) \ "groups" \\ "title").map(_.as[String]) mustBe Seq("ITS web team")
    }

    "return Bad Request for an empty query string" in {
      val controller = new WebGroupsController(groupService, publisherService)

      val result = controller.results("test", " ")(FakeRequest())

      status(result) mustBe 400
      (contentAsJson(result) \ "success").as[Boolean] mustBe false
      (contentAsJson(result) \ "status").as[String] mustBe "bad_request"
    }

    "filter the list of WebGroups by departments accessible to the publisher" in {
      val controller = new WebGroupsController(groupService, publisherService)

      when(publisherService.getPermissionScope("test")).thenReturn(Departments(Seq("CS")))
      when(groupService.getGroupsForQuery("elab")).thenReturn(Success(Seq(
        Fixtures.user.makeGroup(name = "in-elab"),
        Fixtures.user.makeGroup(name = "cs-cs118", title = "Programming for Computer Scientists", department = "CS")
      )))

      val result = controller.results("test", "elab")(FakeRequest())

      status(result) mustBe 200
      (contentAsJson(result) \ "success").as[Boolean] mustBe true
      (contentAsJson(result) \ "status").as[String] mustBe "ok"
      (contentAsJson(result) \ "groups" \\ "name").map(_.as[String]) mustBe Seq("cs-cs118")
      (contentAsJson(result) \ "groups" \\ "title").map(_.as[String]) mustBe Seq("Programming for Computer Scientists")
    }
  }

}
