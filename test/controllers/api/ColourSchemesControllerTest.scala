package controllers.api

import com.typesafe.config.{Config, ConfigFactory}
import helpers.WithActorSystem
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{PlayBodyParsers, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{call, _}
import services._
import warwick.sso._

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ColourSchemesControllerTest extends PlaySpec with MockitoSugar with Results with WithActorSystem {

  val fox: User = Users.create(usercode = Usercode("in-reynard-fox"))
  val FakeLoginUrl = "https://app.example.com/login"


  val mockSSOClientLoggedIn = new MockSSOClient(new LoginContext {
    override val user: Option[User] = Some(fox)
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = FakeLoginUrl

    override def userHasRole(role: RoleName) = true

    override def actualUserHasRole(role: RoleName) = true
  })

  val mockSSOClientLoggedOut = new MockSSOClient(new LoginContext {
    override val user: Option[User] = None
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = FakeLoginUrl

    override def userHasRole(role: RoleName) = false

    override def actualUserHasRole(role: RoleName) = false
  })

  val prefsMock: UserPreferencesService = mock[UserPreferencesService]
  when(prefsMock.getChosenColourScheme(fox.usercode)).thenReturn(ColourScheme(2, false))

  val geeseBackground = Map(
    "id" -> 1,
    "name" -> "Geese invasion",
    "url" -> "geese_westwood.jpg",
    "schemeColour" -> "#ffffff"
  )

  val foxBackground = Map(
    "id" -> 2,
    "name" -> "Fox den",
    "url" -> "fox_den.jpg",
    "schemeColour" -> "#000000"
  )

  val config = Configuration(
    "mywarwick.backgrounds" -> Seq(
      geeseBackground,
      foxBackground
    )
  )

  "ColourSchemesControllerTest#get" should {
    "correctly retrieve fox's chosen colour scheme" in {
      // we're logged in
      val controller = getControllerForTest(loggedIn = true)

      // what does the fox say? Retrieve chosen colour scheme via API
      val result = call(controller.get, FakeRequest())
      status(result) mustBe OK
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "data" \ "chosen").as[Int] mustBe 2
      (json \ "data" \ "schemes").as[Seq[JsValue]].length mustBe 2
      (json \ "data" \ "schemes" \ 0 \ "name").as[String] mustBe "Geese invasion"
      (json \ "data" \ "schemes" \ 0 \ "url").as[String] mustBe "geese_westwood.jpg"
      (json \ "data" \ "schemes" \ 0 \ "schemeColour").as[String] mustBe "#ffffff"
      (json \ "data" \ "schemes" \ 1 \ "name").as[String] mustBe "Fox den"
      (json \ "data" \ "schemes" \ 1 \ "url").as[String] mustBe "fox_den.jpg"
      (json \ "data" \ "schemes" \ 1 \ "schemeColour").as[String] mustBe "#000000"
    }

    "correctly retrieve an anonymous user's colour scheme, the default" in {
      // we're not logged in
      val controller = getControllerForTest(loggedIn = false)

      val result = call(controller.get, FakeRequest())
      status(result) mustBe OK
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "data" \ "chosen").as[Int] mustBe 1
      (json \ "data" \ "schemes").as[Seq[JsValue]].length mustBe 2
      (json \ "data" \ "schemes" \ 0 \ "name").as[String] mustBe "Geese invasion"
      (json \ "data" \ "schemes" \ 0 \ "url").as[String] mustBe "geese_westwood.jpg"
      (json \ "data" \ "schemes" \ 0 \ "schemeColour").as[String] mustBe "#ffffff"
      (json \ "data" \ "schemes" \ 1 \ "name").as[String] mustBe "Fox den"
      (json \ "data" \ "schemes" \ 1 \ "url").as[String] mustBe "fox_den.jpg"
      (json \ "data" \ "schemes" \ 1 \ "schemeColour").as[String] mustBe "#000000"
    }
  }

  "ColourSchemesControllerTest#persist" should {
    "correctly set fox's colour scheme" in {
      // we're logged in
      val controller = getControllerForTest(loggedIn = true)

      val result = call(controller.persist, FakeRequest("POST", "/").withHeaders("Content-Type" -> "application/json").withJsonBody(Json.obj("colourScheme" -> JsNumber(1))))
      status(result) mustBe OK
      verify(prefsMock, atLeastOnce()).setChosenColourScheme(fox.usercode, ColourScheme(1, highContrast = false))

      checkResultAgainstDefault(result)
    }

    "deal with invalid numeric input" in {
      // we're logged in
      val controller = getControllerForTest(loggedIn = true)

      val result = call(controller.persist, FakeRequest("POST", "/").withHeaders("Content-Type" -> "application/json").withJsonBody(Json.obj("colourScheme" -> JsNumber(-1))))
      status(result) mustBe OK
      verify(prefsMock, atLeastOnce()).setChosenColourScheme(fox.usercode, ColourScheme(1, highContrast = false))

      checkResultAgainstDefault(result)
    }

    "deal with invalid non-numeric input" in {
      // we're logged in
      val controller = getControllerForTest(loggedIn = true)

      val result = call(controller.persist, FakeRequest("POST", "/").withHeaders("Content-Type" -> "application/json").withJsonBody(Json.obj("colourScheme" -> JsString("foo"))))
      status(result) mustBe OK
      verify(prefsMock, atLeastOnce()).setChosenColourScheme(fox.usercode, ColourScheme(1, highContrast = false))

      checkResultAgainstDefault(result)
    }
  }

  private def getControllerForTest(loggedIn: Boolean): ColourSchemesController = {
    var secService: SecurityService = null
    if (loggedIn) {
      secService = new SecurityServiceImpl(mockSSOClientLoggedIn, mock[BasicAuth], PlayBodyParsers())
    } else {
      secService = new SecurityServiceImpl(mockSSOClientLoggedOut, mock[BasicAuth], PlayBodyParsers())
    }
    new ColourSchemesController(secService, config, prefsMock)

  }

  private def checkResultAgainstDefault(result: Future[Result]) = {
    val json = contentAsJson(result)

    (json \ "success").as[Boolean] mustBe true
    (json \ "data" \ "id").as[Int] mustBe 1
    (json \ "data" \ "name").as[String] mustBe "Geese invasion"
  }
}
