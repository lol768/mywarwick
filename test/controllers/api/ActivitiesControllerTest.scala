package controllers.api

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.{ActivityService, AppPermissionService, SecurityServiceImpl}
import warwick.sso._

import scala.util.Success

class ActivitiesControllerTest extends PlaySpec with MockitoSugar with Results {

  val tabula = "tabula"
  val ron = Users.create(usercode = Usercode("ron"))

  val ssoClient = new MockSSOClient(new LoginContext {
    override val user: Option[User] = Some(ron)
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = "https://app.example.com/login"
  })

  val appPermissionService = mock[AppPermissionService]
  val activityService = mock[ActivityService]

  val controller = new ActivitiesController(
    new SecurityServiceImpl(ssoClient, mock[BasicAuth], mock[CacheApi]),
    activityService,
    appPermissionService
  )

  "ActivitiesController#postNotification" should {
    val request = FakeRequest().withJsonBody(Json.obj(
      "type" -> "due",
      "title" -> "Coursework due soon",
      "url" -> "http://tabula.warwick.ac.uk",
      "text" -> "Your submission for CS118 is due tomorrow",
      "recipients" -> Json.obj(
        "users" -> Json.arr(
          "someone"
        )
      )
    ))

    "return forbidden when user is not authorised to post for app" in {
      when(appPermissionService.canUserPostForApp(tabula, ron)).thenReturn(false)

      val result = call(controller.postNotification(tabula), request)

      status(result) mustBe FORBIDDEN
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe false
      (json \ "status").as[String] mustBe "forbidden"
      (json \ "errors" \\ "message").map(_.as[String]).head must include("does not have permission")
    }

    "return created activity ID on success" in {
      when(appPermissionService.canUserPostForApp(tabula, ron)).thenReturn(true)
      when(activityService.save(any())).thenReturn(Success("created-activity-id"))

      val result = call(controller.postNotification(tabula), request)

      status(result) mustBe CREATED
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "status").as[String] mustBe "ok"
      (json \ "id").as[String] mustBe "created-activity-id"
    }
  }
}
