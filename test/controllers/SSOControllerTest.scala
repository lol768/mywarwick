package controllers

import org.apache.commons.configuration.BaseConfiguration
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.ac.warwick.sso.client.cache.{UserCache, UserCacheItem}
import uk.ac.warwick.sso.client.{SSOConfiguration, SSOToken}

class SSOControllerTest extends PlaySpec with MockitoSugar with Results {
  val baseConfig = new BaseConfiguration
  val ssoConfig = new SSOConfiguration(baseConfig);
  val userCache = mock[UserCache]

  baseConfig.setProperty("shire.sscookie.name", "Start-SSC")

  val controller = new SSOController(ssoConfig, userCache)

  "SSOController#ssotest" should {
    "handle anonymous" in {
      val result = controller.ssotest(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must be("false")
    }

    "handle unrecognised cookie" in {
      val ssc = Cookie(name="Start-SSC", value="unrecognised")
      val result = controller.ssotest(FakeRequest().withCookies(ssc))
      status(result) must be(200)
      contentAsString(result) must be("true")
    }

    "handle recognised cookie" in {
      val ssc = Cookie(name="Start-SSC", value="recognised")
      val key = new SSOToken("recognised", SSOToken.SSC_TICKET_TYPE)
      // doesn't matter that the cache item is full of nulls, we just check something is returned.
      when(userCache.get(key)).thenReturn(new UserCacheItem(null, 0, null))
      val result = controller.ssotest(FakeRequest().withCookies(ssc))
      status(result) must be(200)
      contentAsString(result) must be("false")
    }
  }
}
