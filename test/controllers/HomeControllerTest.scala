package controllers

import helpers.Fixtures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.{PhotoService, NullSecurityService}
import system.AppMetrics
import warwick.sso._

class HomeControllerTest extends PlaySpec with MockitoSugar with Results {
  val ron = Fixtures.user.makeFoundUser("ron")
  val loginContext = Fixtures.user.loginContext(Option(ron))

  val metrics = mock[AppMetrics]
  val ssoClient = new MockSSOClient(loginContext)
  val securityService = new NullSecurityService(loginContext)
  val photoService = mock[PhotoService]

  val controller = new HomeController(securityService, ssoClient, metrics, photoService)

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
    }
  }
}
