package controllers.publish

import helpers.{BaseSpec, Fixtures}
import models.publishing.PermissionScope.{AllDepartments, Departments}
import models.publishing.Publisher
import models.publishing.PublishingRole.NewsManager
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.cache.CacheApi
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.{PublisherService, SecurityServiceImpl}
import warwick.sso._

import scala.util.Success

class WebGroupsControllerTest extends BaseSpec with MockitoSugar with Results {

  private val groupService = mock[GroupService]
  private val publisherService = mock[PublisherService]

  private val custard = Usercode("custard")

  private val mockSSOClient = new MockSSOClient(new LoginContext {
    override def loginUrl(target: Option[String]) = ""

    override def actualUserHasRole(role: RoleName) = false

    override def userHasRole(role: RoleName) = false

    override val user: Option[User] = Some(Users.create(custard))
    override val actualUser: Option[User] = user
  })

  private val securityService = new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], mock[CacheApi])

  private val controller = new WebGroupsController(groupService, publisherService, securityService)

  when(publisherService.find("test")).thenReturn(Some(Publisher("test", "Test Publisher")))
  when(publisherService.getRoleForUser("test", custard)).thenReturn(NewsManager)

  "WebGroupsController" should {
    "return a list of matching WebGroups" in {
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
      val result = controller.results("test", " ")(FakeRequest())

      status(result) mustBe 400
      (contentAsJson(result) \ "success").as[Boolean] mustBe false
      (contentAsJson(result) \ "status").as[String] mustBe "bad_request"
    }

    "filter the list of WebGroups by departments accessible to the publisher" in {
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
