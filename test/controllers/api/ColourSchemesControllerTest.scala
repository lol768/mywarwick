package controllers.api

import com.typesafe.config.{Config, ConfigFactory}
import helpers.WithActorSystem
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsNumber, JsValue, Json}
import play.api.test.Helpers._
import play.api.test._
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.call
import services._
import warwick.sso._

import collection.JavaConversions._


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
  when(prefsMock.getChosenColourScheme(fox.usercode)).thenReturn(2)

  val confMock: Configuration = mock[Configuration]
  val geeseBackground: Config = ConfigFactory.parseMap(mapAsJavaMap(Map(
    "id" -> 1,
    "name" -> "Geese invasion",
    "url" -> "geese_westwood.jpg"
  )))

  val foxBackground: Config = ConfigFactory.parseMap(mapAsJavaMap(Map(
    "id" -> 2,
    "name" -> "Fox den",
    "url" -> "fox_den.jpg"
  )))

  val configList = new java.util.ArrayList[Configuration]
  configList.add(new Configuration(geeseBackground))
  configList.add(new Configuration(foxBackground))
  when(confMock.getConfigList("mywarwick.backgrounds")).thenReturn(Some(configList))

  "ColourSchemesControllerTest#get" should {

    "correctly retrieve fox's chosen colour scheme" in {
      // we're logged in
      val secService = new SecurityServiceImpl(mockSSOClientLoggedIn, mock[BasicAuth], mock[CacheApi])

      val controller = new ColourSchemesController(secService, confMock, prefsMock)

      // what does the fox say? Retrieve chosen colour scheme via API
      val result = call(controller.get, FakeRequest())
      status(result) mustBe OK
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "data" \ "chosen").as[Int] mustBe 2
      (json \ "data" \ "schemes").as[Seq[JsValue]].length mustBe 2
      (json \ "data" \ "schemes" \ 0 \ "name").as[String] mustBe "Geese invasion"
      (json \ "data" \ "schemes" \ 0 \ "url").as[String] mustBe "geese_westwood.jpg"
      (json \ "data" \ "schemes" \ 1 \ "name").as[String] mustBe "Fox den"
      (json \ "data" \ "schemes" \ 1 \ "url").as[String] mustBe "fox_den.jpg"
    }

    "correctly set fox's colour scheme" in {
      // we're logged in
      val secService = new SecurityServiceImpl(mockSSOClientLoggedIn, mock[BasicAuth], mock[CacheApi])

      val controller = new ColourSchemesController(secService, confMock, prefsMock)
      val result = call(controller.persist, FakeRequest().withHeaders("Content-Type" -> "application/json").withJsonBody(Json.obj("colourScheme" -> JsNumber(1))))
      status(result) mustBe OK
      System.out.println("QQ " + contentAsString(result))
      val json = contentAsJson(result)
      verify(prefsMock.setChosenColourScheme(fox.usercode, 1), times(1))

      (json \ "success").as[Boolean] mustBe true
      (json \ "data" \ "id").as[Int] mustBe 1
      (json \ "data" \ "name").as[String] mustBe "Geese invasion"
    }

  }

}
