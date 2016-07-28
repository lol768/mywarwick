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

class HomeControllerTest extends PlaySpec with MockitoSugar with Results {
  val metrics = mock[AppMetrics]
  val configuration = mock[Configuration]

  when(configuration.getString("start.analytics.tracking-id")).thenReturn(None)
  when(configuration.getString("start.search.root")).thenReturn(Some("https://search-dev.warwick.ac.uk"))

  val controller = new HomeController(metrics, configuration)

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
    }
  }
}
