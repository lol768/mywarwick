package controllers.admin

import akka.stream.ActorMaterializer
import helpers.{OneStartAppPerSuite, TestActors}
import models.PublishingRole.NewsManager
import models.news.Audience
import models.{NewsCategory, Publisher, PublisherPermissionScope, PublishingRole}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test._
import services.dao.{DepartmentInfo, DepartmentInfoDao}
import services.{NewsCategoryService, NewsService, PublisherService, SecurityServiceImpl}
import warwick.sso._

import scala.concurrent.Future

class NewsControllerTest extends PlaySpec with MockitoSugar with Results with OneStartAppPerSuite {

  implicit val akka = TestActors.plainActorSystem()
  implicit val mat = ActorMaterializer()

  val custard = Usercode("custard")

  val securityServiceImpl = new SecurityServiceImpl(new MockSSOClient(new LoginContext {
    override def loginUrl(target: Option[String]) = ""

    override def actualUserHasRole(role: RoleName) = false

    override def userHasRole(role: RoleName) = false

    override val user: Option[User] = Some(Users.create(custard))
    override val actualUser: Option[User] = user
  }), mock[BasicAuth], mock[CacheApi])

  val publisherService = mock[PublisherService]
  val newsService = mock[NewsService]
  val departmentInfoDao = mock[DepartmentInfoDao]
  val newsCategoryService = mock[NewsCategoryService]
  val messagesApi = app.injector.instanceOf[MessagesApi]
  val audienceBinder = mock[AudienceBinder]

  when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("IN", "IT Services", "IT Services", "ITS", "SERVICE")))
  when(newsCategoryService.all()).thenReturn(Seq(NewsCategory("abc", "Campus")))

  val newsController = new NewsController(securityServiceImpl, publisherService, messagesApi, newsService, departmentInfoDao, audienceBinder, newsCategoryService)

  "NewsController#list" should {

    "return Forbidden if user has no permission" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRolesForUser("xyz", custard)).thenReturn(Nil)

      val result = call(newsController.list("xyz"), FakeRequest())

      status(result) mustBe FORBIDDEN
    }

    "return Not Found if publisher does not exist" in {
      when(publisherService.find("xyz")).thenReturn(None)

      val result = call(newsController.list("xyz"), FakeRequest())

      status(result) mustBe NOT_FOUND
    }

    "show a page with no news items" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRolesForUser("xyz", custard)).thenReturn(Seq(NewsManager))

      when(newsService.getNewsByPublisher("xyz", 100)).thenReturn(Nil)

      val result = call(newsController.list("xyz"), FakeRequest())

      status(result) mustBe OK
      contentAsString(result) must include("Create news")
      contentAsString(result) must include("/admin/publish/xyz/news/new")
      contentAsString(result) must include("No published news")
    }

  }

  "NewsController#createForm" should {

    "return Forbidden if user does not have Create permission" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRolesForUser("xyz", custard)).thenReturn(Seq(PublishingRole.Viewer))

      val result = call(newsController.createForm("xyz"), FakeRequest())

      status(result) mustBe FORBIDDEN
    }

    "display news form" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRolesForUser("xyz", custard)).thenReturn(Seq(NewsManager))
      when(publisherService.getPermissionScope("xyz")).thenReturn(PublisherPermissionScope.AllDepartments)

      val result = call(newsController.createForm("xyz"), FakeRequest())

      status(result) mustBe OK
      contentAsString(result) must include("IT Services")
      contentAsString(result) must include("Everyone (public)")
    }

  }

  "NewsController#create" should {

    "create a news item" in {
      val data = Seq(
        "item.title" -> "Big news",
        "item.text" -> "Something happened",
        "item.publishDate" -> "2016-01-01T00:00.000",
        "categories[]" -> "abc",
        "audience.department" -> "IN",
        "audience.audience[]" -> "Dept:Staff"
      )

      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRolesForUser("xyz", custard)).thenReturn(Seq(NewsManager))
      when(publisherService.getPermissionScope("xyz")).thenReturn(PublisherPermissionScope.Departments(Seq("IN")))

      val audience = Audience(Seq(Audience.DepartmentAudience("IN", Seq(Audience.Staff))))
      when(audienceBinder.bindAudience(AudienceData(Seq("Dept:Staff"), Some("IN")))).thenReturn(Future.successful(Right(audience)))

      val result = call(newsController.create("xyz"), FakeRequest("POST", "/").withFormUrlEncodedBody(data: _*))

      redirectLocation(result) must contain("/admin/publish/xyz/news")

      verify(newsService).save(Matchers.any(), Matchers.eq(audience), Matchers.eq(Seq("abc")))
    }

    "not publish to audience without permission" in {
      val data = Seq(
        "item.title" -> "Big news",
        "item.text" -> "Something happened",
        "item.publishDate" -> "2016-01-01T00:00.000",
        "categories[]" -> "abc",
        "audience.audience[]" -> "Public"
      )

      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRolesForUser("xyz", custard)).thenReturn(Seq(NewsManager))
      when(publisherService.getPermissionScope("xyz")).thenReturn(PublisherPermissionScope.Departments(Seq("IN")))

      val result = call(newsController.create("xyz"), FakeRequest("POST", "/").withFormUrlEncodedBody(data: _*))

      contentAsString(result) must include("You do not have the required permissions to publish to that audience.")
    }

  }

}
