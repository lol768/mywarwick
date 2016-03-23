package controllers

import helpers.Fixtures
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.{NullSecurityService, PhotoService}
import system.AppMetrics
import warwick.sso._

class HomeControllerTest extends PlaySpec with MockitoSugar with Results {
  val ron = Fixtures.user.makeFoundUser("ron")
  val loginContext = Fixtures.user.loginContext(Option(ron))

  val metrics = mock[AppMetrics]
  val ssoClient = new MockSSOClient(loginContext)
  val securityService = new NullSecurityService(loginContext)
  val photoService = mock[PhotoService]
  val configuration = mock[Configuration]

  when(configuration.getString("start.analytics.tracking-id")).thenReturn(None)

  val controller = new HomeController(securityService, ssoClient, metrics, photoService, configuration)

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
    }
  }
}
