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
  val securityService = new NullSecurityService(loginContext)
  val photoService = mock[PhotoService]
  val configuration = mock[Configuration]

  when(configuration.getString("start.analytics.tracking-id")).thenReturn(None)
  when(configuration.getString("start.search.root")).thenReturn(Some("https://search-dev.warwick.ac.uk"))

  val controller = new HomeController(securityService, metrics, photoService, configuration)

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
    }
  }
}
