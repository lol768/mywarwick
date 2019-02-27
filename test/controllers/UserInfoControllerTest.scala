package controllers

import helpers._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.Suite
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject._
import play.api.libs.json.{JsString, JsValue}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services._
import services.analytics.AnalyticsMeasurementService
import uk.ac.warwick.sso.client.SSOToken
import uk.ac.warwick.sso.client.cache.{UserCache, UserCacheItem}
import warwick.sso._

import scala.collection.immutable
import scala.concurrent.Future

class UserInfoControllerTest extends BaseSpec with MockitoSugar with Results {
  val HOSTNAME = "example.warwick.ac.uk"
  val REFRESH_URL = "https://signon.example.com/login"
  val LOGIN_URL = s"$REFRESH_URL?permdenied"
  val LOGOUT_URL = s"https://example.warwick.ac.uk/logout?target=https://$HOSTNAME"

  def FakeRequestWithHost() =
    FakeRequest().withHeaders(HeaderNames.HOST -> HOSTNAME)

  val mockPhotoService = mock[PhotoService]
  when(mockPhotoService.photoUrl(any())).thenReturn(Future.failed(new RuntimeException("Fail")))

  val analytics = mock[AnalyticsMeasurementService]
  when(analytics.getUserIdentifier(Usercode("user"))).thenReturn("user-identifier")

  def appBuilder(user: Option[User]) =
    TestApplications.fullNoRoutesBuilder(user)
      .overrides(
        bind[UserInitialisationService].toInstance(mock[UserInitialisationService]),
        bind[NavigationService].toInstance(new MockNavigationService),
        bind[FeaturesService].toInstance(new MockFeaturesService),
        bind[PhotoService].toInstance(mockPhotoService),
        bind[AnalyticsMeasurementService].toInstance(analytics)
      )

  override def nestedSuites = immutable.IndexedSeq[Suite](SignedIn, SignedOut)

  object SignedOut extends OneStartAppPerSuite {
    override lazy val app: Application = appBuilder(None).build()

    def controller = get[UserInfoController]

    "SSOController#info signed out" should {
      "handle anonymous" in {
        val result = controller.info(FakeRequestWithHost())
        status(result) must be(200)
        val json = contentAsJson(result)
        (json \ "refresh").as[Boolean] mustBe false
        (json \ "user" \ "authenticated").as[Boolean] mustBe false
        (json \ "links" \ "login").as[String] mustBe LOGIN_URL
        (json \ "links" \ "logout").as[String] mustBe LOGOUT_URL
      }

      "handle unrecognised cookie" in {
        val ssc = Cookie(name = "SSC-Start-Warwick", value = "unrecognised")
        val result = controller.info(FakeRequestWithHost().withCookies(ssc))
        status(result) must be(200)
        val json = contentAsJson(result)
        (json \ "refresh").as[JsValue] mustBe JsString(REFRESH_URL)
        (json \ "user" \ "authenticated").as[Boolean] mustBe false
        (json \ "links" \ "login").as[String] mustBe LOGIN_URL
        (json \ "links" \ "logout").as[String] mustBe LOGOUT_URL
      }
    }
  }

  object SignedIn extends OneStartAppPerSuite {

    val its = Department(shortName = Some("IT Services"), name = Some("Information Technology Services"), code = Some("IN"))
    val user = Fixtures.user.makeFoundUser().copy(
      department = Some(its),
      rawProperties = Map(
        "warwickitsclass" -> "Staff",
        "warwickyearofstudy" -> "3",
        "warwickfinalyear" -> "true"
      )
    )

    val ssc = Cookie(name = "SSC-Start-Warwick", value = "recognised")
    val key = new SSOToken("recognised", SSOToken.SSC_TICKET_TYPE)

    override lazy val app: Application = appBuilder(Some(user)).build()

    def controller = get[UserInfoController]

    "SSOController#info signed in" should {

      "handle recognised cookie" in {
        get[UserCache].put(key, new UserCacheItem(null, 0, null))

        val result = controller.info(FakeRequestWithHost().withCookies(ssc))
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
        assert((json \ "user" \ "photo" \ "url").as[String].endsWith("/assets/images/no-photo.png"))
      }

      "include photo URL when one is available" in {
        when(mockPhotoService.photoUrl(Some(UniversityID("1234567")))).thenReturn(Future.successful("https://photos/photo/123"))

        get[UserCache].put(key, new UserCacheItem(null, 0, null))

        val result = controller.info(FakeRequestWithHost().withCookies(ssc))
        status(result) must be(200)

        val json = contentAsJson(result)
        (json \ "user" \ "photo" \ "url").as[String] mustBe "https://photos/photo/123"
      }
    }
  }

}
