package controllers

import helpers.Fixtures
import org.apache.commons.configuration.BaseConfiguration
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.analytics.AnalyticsMeasurementService
import services.{PhotoService, UserInitialisationService}
import uk.ac.warwick.sso.client.cache.{UserCache, UserCacheItem}
import uk.ac.warwick.sso.client.{SSOConfiguration, SSOToken}
import warwick.sso._

import scala.concurrent.Future

class UserInfoControllerTest extends PlaySpec with MockitoSugar with Results {
  val baseConfig = new BaseConfiguration
  val ssoConfig = new SSOConfiguration(baseConfig)
  val userCache = mock[UserCache]

  val HOSTNAME = "example.warwick.ac.uk"

  baseConfig.setProperty("shire.sscookie.name", "Start-SSC")
  baseConfig.setProperty("shire.sscookie.path", "/")
  baseConfig.setProperty("shire.sscookie.domain", HOSTNAME)

  val photoService = mock[PhotoService]
  when(photoService.photoUrl(any())).thenReturn(Future.failed(new Exception))

  val measurementService = mock[AnalyticsMeasurementService]
  when(measurementService.getUserIdentifier(Usercode("user"))).thenReturn("user-identifier")

  def controller(user: Option[User] = None) = {
    val loginContext = Fixtures.user.loginContext(user)
    val ssoClient = new MockSSOClient(loginContext)
    new UserInfoController(ssoConfig, userCache, ssoClient, mock[UserInitialisationService], photoService, measurementService)
  }
  val LOGIN_URL = "https://signon.example.com/login"
  val LOGOUT_URL = s"https://example.warwick.ac.uk/logout?target=https://$HOSTNAME"

  def FakeRequestWithHost(hostname:String = HOSTNAME) =
    FakeRequest().withHeaders(HeaderNames.HOST -> hostname)

  "SSOController#info" should {
    "handle anonymous" in {
      val result = controller().info(FakeRequestWithHost())
      status(result) must be(200)
      val json = contentAsJson(result)
      (json \ "refresh").as[Boolean] mustBe false
      (json \ "user" \ "authenticated").as[Boolean] mustBe false
      (json \ "links" \ "login").as[String] mustBe LOGIN_URL
      (json \ "links" \ "logout").as[String] mustBe LOGOUT_URL
    }

    "handle unrecognised cookie" in {
      val ssc = Cookie(name = "Start-SSC", value = "unrecognised")
      val result = controller().info(FakeRequestWithHost().withCookies(ssc))
      status(result) must be(200)
      val json = contentAsJson(result)
      (json \ "refresh").as[String] mustBe LOGIN_URL
      (json \ "user" \ "authenticated").as[Boolean] mustBe false
      (json \ "links" \ "login").as[String] mustBe LOGIN_URL
      (json \ "links" \ "logout").as[String] mustBe LOGOUT_URL
    }

    "handle recognised cookie" in {
      val ssc = Cookie(name = "Start-SSC", value = "recognised")
      val key = new SSOToken("recognised", SSOToken.SSC_TICKET_TYPE)
      // doesn't matter that the cache item is full of nulls, we just check something is returned.
      when(userCache.get(key)).thenReturn(new UserCacheItem(null, 0, null))

      val its = Department(shortName = Some("IT Services"), name = Some("Information Technology Services"), code = Some("IN"))

      val user = Fixtures.user.makeFoundUser().copy(
        department = Some(its),
        rawProperties = Map(
          "warwickitsclass" -> "Staff",
          "warwickyearofstudy" -> "3",
          "warwickfinalyear" -> "true"
        )
      )

      val result = controller(Some(user)).info(FakeRequestWithHost().withCookies(ssc))
      status(result) must be(200)

      val json = contentAsJson(result)
      (json \ "refresh").as[Boolean] mustBe false
      (json \ "user" \ "authenticated").as[Boolean] mustBe true
      (json \ "user" \ "usercode").as[String] mustBe "user"
      (json \ "user" \ "analytics" \ "identifier").as[String] mustBe "user-identifier"
      (json \ "user" \ "analytics" \ "dimensions" \\ "index").map(_.as[Int]) mustBe Seq(1, 2, 3, 4)
      (json \ "user" \ "analytics" \ "dimensions" \\ "value").map(_.as[String]) mustBe Seq("IT Services", "Staff", "3", "true")
      (json \ "links" \ "login").as[String] mustBe LOGIN_URL
      (json \ "links" \ "logout").as[String] mustBe LOGOUT_URL
      (json \ "user" \ "photo" \ "url").as[String] mustBe "/assets/images/no-photo.png"
    }

    "include photo URL when one is available" in {
      val ssc = Cookie(name = "Start-SSC", value = "recognised")
      val key = new SSOToken("recognised", SSOToken.SSC_TICKET_TYPE)
      // doesn't matter that the cache item is full of nulls, we just check something is returned.
      when(userCache.get(key)).thenReturn(new UserCacheItem(null, 0, null))

      when(photoService.photoUrl(Some(UniversityID("1234567")))).thenReturn(Future.successful("https://photos/photo/123"))

      val user = Fixtures.user.makeFoundUser()
      val result = controller(Some(user)).info(FakeRequestWithHost().withCookies(ssc))
      status(result) must be(200)

      val json = contentAsJson(result)
      (json \ "user" \ "photo" \ "url").as[String] mustBe "https://photos/photo/123"
    }
  }
}
