package controllers

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import helpers.BaseSpec
import org.mockito.Matchers.any
import play.api.Configuration
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.{NullSecurityService, SecurityService, UserPreferencesService}
import services.analytics.{AnalyticsMeasurementService, AnalyticsTrackingID}
import system.AppMetrics
import warwick.sso._

class HomeControllerTest extends BaseSpec with MockitoSugar with Results {

  val securityService = new NullSecurityService(new LoginContext {
    override def loginUrl(target: Option[String]) = ""

    override def actualUserHasRole(role: RoleName) = false

    override def userHasRole(role: RoleName) = false

    override val user: Option[User] = Some(Users.create(Usercode("test")))
    override val actualUser: Option[User] = user
  })

  val metrics = mock[AppMetrics]

  val configuration = mock[Configuration]
  when(configuration.getString("mywarwick.search.root")).thenReturn(Some("https://search-dev.warwick.ac.uk"))
  when(configuration.getBoolean("mywarwick.showBetaWarning")).thenReturn(Some(false))

  val measurementService = mock[AnalyticsMeasurementService]
  when(measurementService.trackingID).thenReturn(AnalyticsTrackingID("UA-123456-7"))

  val userPrefService = mock[UserPreferencesService]
  when(userPrefService.getChosenColourScheme(any())).thenReturn(1)

  val controller = new HomeController(securityService, metrics, configuration, measurementService, userPrefService)

  "ApplicationController#index" should {
    "render" in {
      val result = controller.index(FakeRequest())
      status(result) must be(200)
      contentAsString(result) must include("id=\"app-container\"")
    }
  }
}
