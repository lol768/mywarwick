package controllers.admin

import helpers.{Fixtures, OneStartAppPerSuite}
import models.Audience.Staff
import models.publishing.PublishingRole.NewsManager
import models.publishing._
import models.{Audience, NewsCategory}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test._
import services._
import services.dao.{DepartmentInfo, DepartmentInfoDao}
import system.ErrorHandler
import warwick.sso._

import scala.concurrent.Future
import scala.util.Success

class NewsControllerTest extends PlaySpec with MockitoSugar with Results with OneStartAppPerSuite {

  val custard = Usercode("custard")

  val mockSSOClient = new MockSSOClient(new LoginContext {
    override def loginUrl(target: Option[String]) = ""
    override def actualUserHasRole(role: RoleName) = false
    override def userHasRole(role: RoleName) = false

    override val user: Option[User] = Some(Users.create(custard))
    override val actualUser: Option[User] = user
  })

  val securityServiceImpl = new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], mock[CacheApi])

  val publisherService = mock[PublisherService]
  val newsService = mock[NewsService]
  val departmentInfoDao = mock[DepartmentInfoDao]
  val newsCategoryService = mock[NewsCategoryService]
  val messagesApi = app.injector.instanceOf[MessagesApi]
  val audienceBinder = mock[AudienceBinder]
  val audienceService = mock[AudienceService]
  val userPreferencesService = mock[UserPreferencesService]
  val userNewsCategoryService = mock[UserNewsCategoryService]

  when(departmentInfoDao.allDepartments).thenReturn(Seq(DepartmentInfo("IN", "IT Services", "IT Services", "ITS", "SERVICE")))
  when(newsCategoryService.all()).thenReturn(Seq(NewsCategory("abc", "Campus")))

  val newsController = new NewsController(securityServiceImpl, publisherService, messagesApi, newsService, departmentInfoDao, audienceBinder, newsCategoryService, userNewsCategoryService, mock[ErrorHandler], audienceService, userPreferencesService) {
    override val navigationService = new MockNavigationService()
    override val ssoClient = mockSSOClient
  }

  "NewsController#list" should {

    "return Forbidden if user has no permission" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NullRole)

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
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)

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
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(PublishingRole.Viewer)

      val result = call(newsController.createForm("xyz"), FakeRequest())

      status(result) mustBe FORBIDDEN
    }

    "display news form" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.AllDepartments)

      val result = call(newsController.createForm("xyz"), FakeRequest())

      status(result) mustBe OK
      contentAsString(result) must include("IT Services")
      contentAsString(result) must include("Everyone (public)")
    }

  }

  val validData = Seq(
    "item.title" -> "Big news",
    "item.text" -> "Something happened",
    "item.publishDate" -> "2016-01-01T00:00.000",
    "categories[]" -> "abc",
    "audience.department" -> "IN",
    "audience.audience[]" -> "Dept:Staff"
  )

  "NewsController#create" should {

    when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
    when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
    when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))

    "create a news item" in {
      val audience = Audience(Seq(Audience.DepartmentAudience("IN", Seq(Audience.Staff))))
      when(audienceBinder.bindAudience(AudienceData(Seq("Dept:Staff"), Some("IN")))).thenReturn(Future.successful(Right(audience)))

      val result = call(newsController.create("xyz", submitted = true), FakeRequest("POST", "/").withFormUrlEncodedBody(validData: _*))

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
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))

      val result = call(newsController.create("xyz", submitted = true), FakeRequest("POST", "/").withFormUrlEncodedBody(data: _*))

      contentAsString(result) must include("You do not have the required permissions to publish to that audience.")
    }

    "do nothing unless submitted" in {
      reset(newsService)

      val audience = Audience(Seq(Audience.DepartmentAudience("IN", Seq(Audience.Staff))))
      when(audienceBinder.bindAudience(AudienceData(Seq("Dept:Staff"), Some("IN")))).thenReturn(Future.successful(Right(audience)))

      val result = call(newsController.create("xyz", submitted = false), FakeRequest("POST", "/").withFormUrlEncodedBody(validData: _*))

      contentAsString(result) must include("Big news")

      verify(newsService, never()).save(Matchers.any(), Matchers.eq(audience), Matchers.eq(Seq("abc")))
    }

  }

  "NewsController#updateForm" should {

    "return Not Found if the news item does not exist" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))
      when(newsService.getNewsItem("news")).thenReturn(None)

      val result = call(newsController.updateForm("xyz", "news"), FakeRequest())

      status(result) must be(NOT_FOUND)
      contentAsString(result) must include("Sorry, there's nothing at this URL.")
    }

    "return Not Found if the news item does not belong to the requested publisher" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))
      when(newsService.getNewsItem("news")).thenReturn(Some(Fixtures.news.render.copy(publisherId = "another")))

      val result = call(newsController.updateForm("xyz", "news"), FakeRequest())

      status(result) must be(NOT_FOUND)
      contentAsString(result) must include("Sorry, there's nothing at this URL.")
    }

    "display the update form" in {
      when(publisherService.find("xyz")).thenReturn(Some(Publisher("xyz", "Test Publisher")))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))
      when(newsCategoryService.getNewsCategories("news")).thenReturn(Seq.empty)
      when(newsService.getAudience("news")).thenReturn(Some(Audience.Public))
      when(newsService.getNewsItem("news")).thenReturn(Some(Fixtures.news.render.copy(publisherId = "xyz")))

      val result = call(newsController.updateForm("xyz", "news"), FakeRequest())

      status(result) must be(OK)
      contentAsString(result) must include("Update news")
    }

  }

  "NewsController#audienceInfo" should {

    "respond with audience size" in {
      when(audienceService.resolve(Audience(Seq(Audience.DepartmentAudience("IN", Seq(Staff)))))).thenReturn(Success(Seq("a", "b", "c").map(Usercode)))
      when(userPreferencesService.countInitialisedUsers(Seq("a", "b", "c").map(Usercode))).thenReturn(2)
      when(userNewsCategoryService.getRecipientsOfNewsInCategories(Seq("abc"))).thenReturn(Seq("a", "e").map(Usercode))

      val result = call(newsController.audienceInfo("xyz"), FakeRequest("POST", "/").withFormUrlEncodedBody(validData: _*))

      status(result) must be(OK)
      val json = contentAsJson(result)

      (json \ "status").as[String] must be("ok")
      (json \ "data" \ "baseAudience").as[Int] must be(3)
      (json \ "data" \ "categorySubset").as[Int] must be(2)
    }

    "respond with public" in {
      when(audienceBinder.bindAudience(AudienceData(Seq("Public"), None))).thenReturn(Future.successful(Right(Audience.Public)))
      when(audienceService.resolve(Audience.Public)).thenReturn(Success(Seq(Usercode("*"))))
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.AllDepartments)

      val data = Seq(
        "item.title" -> "Big news",
        "item.text" -> "Something happened",
        "item.publishDate" -> "2016-01-01T00:00.000",
        "categories[]" -> "abc",
        "audience.audience[]" -> "Public"
      )

      val result = call(newsController.audienceInfo("xyz"), FakeRequest("POST", "/").withFormUrlEncodedBody(data: _*))

      status(result) must be(OK)
      val json = contentAsJson(result)

      (json \ "status").as[String] must be("ok")
      (json \ "data" \ "public").as[Boolean] must be(true)
    }

  }

}
