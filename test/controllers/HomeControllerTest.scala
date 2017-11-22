package controllers

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import helpers.{BaseSpec, MinimalAppPerSuite}
import play.api.Configuration
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.analytics.{AnalyticsMeasurementService, AnalyticsTrackingID}
import system.AppMetrics

class HomeControllerTest extends BaseSpec with MockitoSugar with Results with MinimalAppPerSuite {
  val metrics = mock[AppMetrics]

  val configuration = Configuration(
    "mywarwick.search.root" -> "https://search-dev.warwick.ac.uk"
  )

  val measurementService = mock[AnalyticsMeasurementService]
  when(measurementService.trackingID).thenReturn(AnalyticsTrackingID("UA-123456-7"))

  val controller = new HomeController(metrics, configuration, measurementService)
  controller.setControllerComponents(get[ControllerComponents])

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
    }
  }
}
