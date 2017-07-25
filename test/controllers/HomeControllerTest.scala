package controllers

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import helpers.BaseSpec
import play.api.Configuration
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.analytics.{AnalyticsMeasurementService, AnalyticsTrackingID}
import system.AppMetrics

class HomeControllerTest extends BaseSpec with MockitoSugar with Results {
  val metrics = mock[AppMetrics]

  val configuration = mock[Configuration]
  when(configuration.getString("mywarwick.search.root")).thenReturn(Some("https://search-dev.warwick.ac.uk"))
  when(configuration.getBoolean("mywarwick.showBetaWarning")).thenReturn(Some(false))

  val measurementService = mock[AnalyticsMeasurementService]
  when(measurementService.trackingID).thenReturn(AnalyticsTrackingID("UA-123456-7"))

  val controller = new HomeController(metrics, configuration, measurementService)

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
    }
  }
}
