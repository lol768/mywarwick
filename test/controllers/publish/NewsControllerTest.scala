package controllers.publish

import controllers.MockFeaturesService
import helpers.{BaseSpec, Fixtures, MinimalAppPerSuite, OneStartAppPerSuite}
import models.Audience.Staff
import models.publishing.PublishingRole.NewsManager
import models.publishing._
import models.{Audience, NewsCategory}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi
import play.api.mvc.{ControllerComponents, PlayBodyParsers, Results}
import play.api.test.Helpers._
import play.api.test._
import play.filters.csrf.CSRF
import play.filters.csrf.CSRF.Token
import play.twirl.api.Html
import services._
import services.dao.DepartmentInfo
import system.{CSRFPageHelper, CSRFPageHelperFactory, ErrorHandler}
import warwick.sso._

import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

class NewsControllerTest extends BaseSpec with MockitoSugar with Results with MinimalAppPerSuite {

  val custard = Usercode("custard")

  val mockSSOClient = new MockSSOClient(new LoginContext {
    override def loginUrl(target: Option[String]) = ""

    override def actualUserHasRole(role: RoleName) = false

    override def userHasRole(role: RoleName) = false

    override val user: Option[User] = Some(Users.create(custard))
    override val actualUser: Option[User] = user
  })

  val securityServiceImpl = new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], PlayBodyParsers())

  private val publisherService = mock[PublisherService]
  private val newsService = mock[NewsService]
  private val departmentInfoService = mock[DepartmentInfoService]
  private val newsCategoryService = mock[NewsCategoryService]
  private val messagesApi = app.injector.instanceOf[MessagesApi]
  private val audienceBinder = mock[AudienceBinder]
  private val audienceService = mock[AudienceService]
  private val userPreferencesService = mock[UserPreferencesService]
  private val userNewsCategoryService = mock[UserNewsCategoryService]

  when(departmentInfoService.allDepartments).thenReturn(Seq(DepartmentInfo("IN", "IT Services", "IT Services", "ITS", "SERVICE", "X")))
  when(departmentInfoService.allPublishableDepartments).thenReturn(Seq(DepartmentInfo("IN", "IT Services", "IT Services", "ITS", "SERVICE", "X")))
  when(newsCategoryService.all()).thenReturn(Seq(NewsCategory("abc", "Campus")))

  private val mockCsrfHelper = mock[CSRFPageHelper]
  private val mockCsrfPageHelperFactory = mock[CSRFPageHelperFactory]


  when(mockCsrfHelper.token).thenReturn(Some(CSRF.Token("Name", "TokenValue")))
  when(mockCsrfHelper.formField()).thenReturn(Html(s"""<input type="hidden" name="Name" value="TokenValue">"""))
  when(mockCsrfHelper.metaElementHeader()).thenReturn(Html(s"""<meta name="_csrf_header" content="Csrf-Token"/>"""))
  when(mockCsrfHelper.metaElementToken()).thenReturn(Html(s"""<meta name="_csrf" content="TokenValue"/>"""))

  when(mockCsrfPageHelperFactory.getInstance(Matchers.any[Option[Token]])).thenReturn(mockCsrfHelper)

  val newsController = new NewsController(securityServiceImpl, publisherService, newsService, departmentInfoService, audienceBinder, newsCategoryService, userNewsCategoryService, mock[ErrorHandler], audienceService, userPreferencesService) {
    override val navigationService = new MockNavigationService()
    override val ssoClient: MockSSOClient = mockSSOClient
    override val csrfPageHelperFactory: CSRFPageHelperFactory = mockCsrfPageHelperFactory
    override val features = new MockFeaturesService

    setControllerComponents(get[ControllerComponents])
  }

  private val publisher = Publisher("xyz", "Test Publisher")

  "NewsController#list" should {

    "return Forbidden if user has no permission" in {
      when(publisherService.find("xyz")).thenReturn(Some(publisher))
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
      when(publisherService.find("xyz")).thenReturn(Some(publisher))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)

      when(newsService.getNewsByPublisherWithAuditsAndAudience("xyz", 100)).thenReturn(Nil)

      val result = call(newsController.list("xyz"), FakeRequest())

      status(result) mustBe OK
      contentAsString(result) must include("Create news")
      contentAsString(result) must include("/publish/xyz/news/new")
      contentAsString(result) must include("No published news")
    }

  }

  "NewsController#createForm" should {

    "return Forbidden if user does not have Create permission" in {
      when(publisherService.find("xyz")).thenReturn(Some(publisher))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(PublishingRole.Viewer)

      val result = call(newsController.createForm("xyz"), FakeRequest())

      status(result) mustBe FORBIDDEN
    }


    "display news form" in {
      when(publisherService.find("xyz")).thenReturn(Some(publisher))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.AllDepartments)

      val result = call(newsController.createForm("xyz"), FakeRequest())

      status(result) mustBe OK
      contentAsString(result) must include("IT Services")
      contentAsString(result) must include("Link URL")
      contentAsString(result) must include("Title")
      contentAsString(result) must include("Text")
      // data attribute with publisher's department options
      contentAsString(result) must include(
        "data-departments='{&quot;IN&quot;:{&quot;name&quot;:&quot;IT Services&quot;,&quot;faculty&quot;:&quot;X&quot;}}'"
      )
      contentAsString(result) must include("class=\"audience-picker\"")
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

    when(publisherService.find("xyz")).thenReturn(Some(publisher))
    when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
    when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))

    "create a news item" in {
      val audience = Audience(Seq(Audience.DepartmentAudience("IN", Seq(Audience.Staff))))
      when(
        audienceBinder.bindAudience(
          Matchers.eq(AudienceData(Seq("Dept:Staff"), Some("IN"))),
          Matchers.eq(false)
        )(Matchers.any())
      ).thenReturn(Future.successful(Right(audience)))

      val result = call(newsController.create("xyz", submitted = true), FakeRequest("POST", "/").withFormUrlEncodedBody(validData: _*))

      redirectLocation(result) must contain("/publish/xyz/news")

      verify(newsService).save(Matchers.any(), Matchers.eq(audience), Matchers.eq(Seq("abc")))
    }

    "not publish to audience without permission" in {
      val data = Seq(
        "item.title" -> "Big news",
        "item.text" -> "Something happened",
        "item.publishDate" -> "2016-01-01T00:00.000",
        "categories[]" -> "abc",
        "audience.audience[]" -> "Dept:TeachingStaff",
        "audience.department" -> "EN"
      )

      when(publisherService.find("xyz")).thenReturn(Some(publisher))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))

      val result = call(newsController.create("xyz", submitted = true), FakeRequest("POST", "/").withFormUrlEncodedBody(data: _*))

      contentAsString(result) must include("You do not have the required permissions to publish to that audience.")
    }

    "do nothing unless submitted" in {
      reset(newsService)

      val audience = Audience(Seq(Audience.DepartmentAudience("IN", Seq(Audience.Staff))))
      when(
        audienceBinder.bindAudience(
          Matchers.eq(AudienceData(Seq("Dept:Staff"), Some("IN"))),
          Matchers.eq(false)
        )(Matchers.any())
      ).thenReturn(Future.successful(Right(audience)))

      val result = call(newsController.create("xyz", submitted = false), FakeRequest("POST", "/").withFormUrlEncodedBody(validData: _*))

      contentAsString(result) must include("Big news")

      verify(newsService, never()).save(Matchers.any(), Matchers.eq(audience), Matchers.eq(Seq("abc")))
    }

  }

  "NewsController#updateForm" should {

    "return Not Found if the news item does not exist" in {
      when(publisherService.find("xyz")).thenReturn(Some(publisher))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))
      when(newsService.getNewsItem("news")).thenReturn(None)

      val result = call(newsController.updateForm("xyz", "news"), FakeRequest())

      status(result) must be(NOT_FOUND)
      contentAsString(result) must include("Sorry, there's nothing at this URL.")
    }

    "return Not Found if the news item does not belong to the requested publisher" in {
      when(publisherService.find("xyz")).thenReturn(Some(publisher))
      when(publisherService.getRoleForUser("xyz", custard)).thenReturn(NewsManager)
      when(publisherService.getPermissionScope("xyz")).thenReturn(PermissionScope.Departments(Seq("IN")))
      when(newsService.getNewsItem("news")).thenReturn(Some(Fixtures.news.render.copy(publisherId = "another")))

      val result = call(newsController.updateForm("xyz", "news"), FakeRequest())

      status(result) must be(NOT_FOUND)
      contentAsString(result) must include("Sorry, there's nothing at this URL.")
    }

    "display the update form" in {
      when(publisherService.find("xyz")).thenReturn(Some(publisher))
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
      val audience = Audience(Seq(Audience.DepartmentAudience("IN", Seq(Staff))))
      when(
        audienceBinder.bindAudience(
          Matchers.eq(AudienceData(Seq("Dept:Staff"), Some("IN"))),
          Matchers.eq(false)
        )(Matchers.any())
      ).thenReturn(Future.successful(Right(audience)))
      when(audienceService.resolve(audience)).thenReturn(Success(Set("a", "b", "c").map(Usercode)))
      when(audienceService.resolveUsersForComponentsGrouped(audience.components)).thenReturn(Success(Seq((Audience.DepartmentAudience("IN", Seq(Staff)), Set("a", "b", "c").map(Usercode)))))
      when(userPreferencesService.countInitialisedUsers(Set("a", "b", "c").map(Usercode))).thenReturn(2)
      when(userNewsCategoryService.getRecipientsOfNewsInCategories(Seq("abc"))).thenReturn(Set("a", "e").map(Usercode))
      when(newsCategoryService.getNewsCategoryForCatId("abc")).thenReturn(NewsCategory("abc", "Campus"))

      val result = call(newsController.audienceInfo("xyz"), FakeRequest("POST", "/").withFormUrlEncodedBody(validData: _*))

      status(result) must be(OK)
      val json = contentAsJson(result)

      (json \ "status").as[String] must be("ok")
      (json \ "data" \ "baseAudience").as[Int] must be(1)
    }

    "respond with public" in {
      when(
        audienceBinder.bindAudience(
          Matchers.eq(AudienceData(Seq("Public"), None)),
          Matchers.eq(false)
        )(Matchers.any())
      ).thenReturn(Future.successful(Right(Audience.Public)))
      when(audienceService.resolve(Audience.Public)).thenReturn(Success(Set(Usercode("*"))))
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
