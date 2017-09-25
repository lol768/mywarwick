package controllers.api

import helpers.WithActorSystem
import models.Audience
import models.publishing.PublishingRole.{APINotificationsManager, NotificationsManager}
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import helpers.BaseSpec
import models.publishing.Publisher
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services._
import warwick.sso._

import scala.util.Try

class IncomingActivitiesControllerTest extends BaseSpec with MockitoSugar with Results with WithActorSystem {

  val tabula = "tabula"
  val tabulaPublisherId = "tabulaPublisherId"
  val tabulaPublisher = Publisher(tabulaPublisherId, "Tabula")
  val ron: User = Users.create(usercode = Usercode("ron"))

  val mockSSOClient = new MockSSOClient(new LoginContext {
    override val user: Option[User] = Some(ron)
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = "https://app.example.com/login"

    override def userHasRole(role: RoleName) = false

    override def actualUserHasRole(role: RoleName) = false
  })

  val publisherService: PublisherService = mock[PublisherService]
  val activityService: ActivityService = mock[ActivityService]
  val audienceService: AudienceService = mock[AudienceService]

  val controller = new IncomingActivitiesController(
    new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], mock[CacheApi]),
    activityService,
    publisherService,
    audienceService,
    mock[MessagesApi]
  ) {
    override val navigationService = new MockNavigationService()
    override val ssoClient: MockSSOClient = mockSSOClient
  }

  val body: JsObject = Json.obj(
    "type" -> "due",
    "title" -> "Coursework due soon",
    "url" -> "http://tabula.warwick.ac.uk",
    "text" -> "Your submission for CS118 is due tomorrow",
    "recipients" -> Json.obj(
      "users" -> Json.arr(
        "someone"
      )
    )
  )

  "IncomingActivitiesController#postNotification" should {

    when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
    when(publisherService.find(tabulaPublisherId)).thenReturn(Some(tabulaPublisher))

    "return forbidden when user is not authorised to post on behalf of Publisher" in {
      when(publisherService.getRoleForUser(tabulaPublisherId, ron.usercode)).thenReturn(NotificationsManager) // requires APINotificationsManager role

      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(body))

      status(result) mustBe FORBIDDEN
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe false
      (json \ "status").as[String] mustBe "forbidden"
      (json \ "errors" \\ "message").map(_.as[String]).head must include("does not have permission")
    }

    "return created activity ID on success" in {
      when(publisherService.getRoleForUser(Matchers.eq(tabulaPublisherId), any())).thenReturn(APINotificationsManager)
      when(activityService.save(any(), Matchers.eq(Audience.usercode(Usercode("someone"))))).thenReturn(Right("created-activity-id"))

      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(body))

      status(result) mustBe CREATED
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "status").as[String] mustBe "ok"
      (json \ "data" \ "id").as[String] mustBe "created-activity-id"
    }

    "send to a webgroup" in {
      val groupAudience = Audience(Seq(Audience.WebGroupAudience(GroupName("in-trigue"))))

      when(publisherService.getRoleForUser(Matchers.eq(tabulaPublisherId), any())).thenReturn(APINotificationsManager)
      when(activityService.save(any(), Matchers.eq(groupAudience))).thenReturn(Right("created-activity-id"))

      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(
        body + ("recipients" -> Json.obj(
          "groups" -> Json.arr(
            "in-trigue"
          )
        ))
      ))

      status(result) mustBe CREATED
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "status").as[String] mustBe "ok"
      (json \ "data" \ "id").as[String] mustBe "created-activity-id"
    }

    "accept generated_at date in the correct format" in {
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(
        body + ("generated_at" -> JsString("2016-01-01T09:00:00.000Z"))
      ))

      status(result) mustBe CREATED

      val otherResult = call(controller.postNotification(tabula), FakeRequest().withJsonBody(
        body + ("generated_at" -> JsString("2016-01-01T09:00:00Z"))
      ))

      status(otherResult) mustBe CREATED
    }

    "reject an incorrectly-formatted generated_at date" in {
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(
        body + ("generated_at" -> JsString("yesterday"))
      ))

      status(result) mustBe BAD_REQUEST
    }

    "fail for invalid provider id" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(None)

      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(body))

      status(result) mustBe BAD_REQUEST
      val json = contentAsJson(result)

      (json \ "errors" \ 0 \ "message").as[String] mustBe s"No provider found with id '$tabula'"
    }

    "fail for invalid usercode" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(Json.obj(
        "type" -> "due",
        "title" -> "Coursework due soon",
        "url" -> "http://tabula.warwick.ac.uk",
        "text" -> "Your submission for CS118 is due tomorrow",
        "recipients" -> Json.obj(
          "users" -> Json.arr(
            "    "
          )
        )
      )))

      status(result) mustBe BAD_REQUEST
      val json = contentAsJson(result)

      (json \ "errors" \ 0 \ "message").as[String] mustBe "All usercodes must be non-empty"
    }

    "fail for too many recipients" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      when(publisherService.find(tabulaPublisherId)).thenReturn(Some(tabulaPublisher.copy(maxRecipients = Some(1))))
      when(audienceService.resolve(Audience(Seq(Audience.UsercodesAudience(Set(Usercode("cusfal"), Usercode("cusebr"))))))).thenReturn(Try(Seq(Usercode("cusfal"), Usercode("cusebr"))))
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(Json.obj(
        "type" -> "due",
        "title" -> "Coursework due soon",
        "url" -> "http://tabula.warwick.ac.uk",
        "text" -> "Your submission for CS118 is due tomorrow",
        "recipients" -> Json.obj(
          "users" -> Json.arr(
            "cusfal",
            "cusebr"
          )
        )
      )))

      status(result) mustBe BAD_REQUEST
      val json = contentAsJson(result)

      (json \ "errors" \ 0 \ "message").as[String] mustBe "You can only send to 1 recipients at a time"
    }
  }
}
