package controllers

import helpers.Fixtures
import org.apache.commons.configuration.BaseConfiguration
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.SecurityServiceImpl
import uk.ac.warwick.sso.client.cache.{UserCache, UserCacheItem}
import uk.ac.warwick.sso.client.{SSOConfiguration, SSOToken}
import warwick.sso._

class SSOControllerTest extends PlaySpec with MockitoSugar with Results {
  val baseConfig = new BaseConfiguration
  val ssoConfig = new SSOConfiguration(baseConfig)
  val userCache = mock[UserCache]

  baseConfig.setProperty("shire.sscookie.name", "Start-SSC")
  baseConfig.setProperty("shire.sscookie.path", "/")
  baseConfig.setProperty("shire.sscookie.domain", "example.warwick.ac.uk")

  def controller(user: Option[User] = None) = {
    val loginContext = Fixtures.user.loginContext(user)
    val ssoClient = new MockSSOClient(loginContext)
    val securityService = new SecurityServiceImpl(ssoClient, mock[BasicAuth], mock[CacheApi])
    new SSOController(ssoConfig, userCache, securityService, ssoClient)
  }

  val LOGIN_URL = "https://signon.example.com/login"
  val LOGOUT_URL = "https://signon.example.com/logout"

  "SSOController#info" should {
    "handle anonymous" in {
      val result = controller().info(FakeRequest())
      status(result) must be(200)
      val json = contentAsJson(result)
      (json \ "refresh").as[Boolean] mustBe false
      (json \ "user" \ "authenticated").as[Boolean] mustBe false
      (json \ "links" \ "login").as[String] mustBe LOGIN_URL
      (json \ "links" \ "logout").as[String] mustBe LOGOUT_URL
    }

    "handle unrecognised cookie" in {
      val ssc = Cookie(name = "Start-SSC", value = "unrecognised")
      val result = controller().info(FakeRequest().withCookies(ssc))
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

      val user = Fixtures.user.makeFoundUser()
      val result = controller(Some(user)).info(FakeRequest().withCookies(ssc))
      status(result) must be(200)

      val json = contentAsJson(result)
      (json \ "refresh").as[Boolean] mustBe false
      (json \ "user" \ "authenticated").as[Boolean] mustBe true
      (json \ "user" \ "usercode").as[String] mustBe "user"
      (json \ "links" \ "login").as[String] mustBe LOGIN_URL
      (json \ "links" \ "logout").as[String] mustBe LOGOUT_URL
    }
  }
}
