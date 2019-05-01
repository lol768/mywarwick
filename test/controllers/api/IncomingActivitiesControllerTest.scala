package controllers.api

import actors.MessageProcessing.ProcessingResult
import helpers.{BaseSpec, MinimalAppPerSuite}
import models.publishing.Publisher
import models.publishing.PublishingRole.{APINotificationsManager, NotificationsManager}
import models.{ActivityProvider, Audience}
import org.mockito.ArgumentMatchers.{eq => isEq, _}
import org.mockito.Mockito._
import org.mockito.ArgumentCaptor
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services._
import services.messaging._
import system.AuditLogContext
import warwick.sso._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class IncomingActivitiesControllerTest extends BaseSpec with MockitoSugar with Results with MinimalAppPerSuite {

  val tabula = "tabula"
  val tabulaPublisherId = "tabulaPublisherId"
  val tabulaPublisher = Publisher(tabulaPublisherId, "Tabula")
  val tabulaProvider: ActivityProvider = ActivityProvider(tabula, sendEmail = false, overrideMuting = false)

  val transientPushProviderId = "transientPushProviderId"
  val transientPushPublisherId = "transientPushPublisherId"
  val transientPushPublisher = Publisher(transientPushPublisherId, "Transient Push")
  val transientPushProvider: ActivityProvider = ActivityProvider(transientPushProviderId, sendEmail = false, transientPush = true, overrideMuting = false)

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
  val messagingService: MessagingService = mock[MessagingService]

  val controller: IncomingActivitiesController = new IncomingActivitiesController(
    new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], PlayBodyParsers()),
    activityService,
    publisherService,
    audienceService,
    messagingService,
    mock[MobileOutputService]
  ) {
    override val navigationService = new MockNavigationService()
    override val ssoClient: MockSSOClient = mockSSOClient

    setControllerComponents(get[ControllerComponents])
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

  "IncomingActivitiesController#transientPushNotification" should {
    when(publisherService.getParentPublisherId(transientPushProviderId)).thenReturn(Some(transientPushPublisherId))
    when(publisherService.getRoleForUser(transientPushPublisherId, ron.usercode)).thenReturn(APINotificationsManager)
    when(activityService.getProvider(transientPushProviderId)).thenReturn(Some(transientPushProvider))

    "parse IncomingTransientPushData to PushNotification model" in {
      val transientPushBody: JsObject = Json.obj(
        "type" -> "two_step_code",
        "title" -> "Your two-step code is hidden inside your phone",
        "priority" -> "high",
        "ttl" -> 240,
        "channel" -> "two_step_codes",
        "url" -> "http://websignon.warwick.ac.uk",
        "recipients" -> Json.obj(
          "users" -> Json.arr(
            "someone"
          )
        )
      )
      when(messagingService.processTransientPushNotification(any[Set[Usercode]], any[PushNotification])(any[AuditLogContext])).thenReturn(
        Future.successful(ProcessingResult(success = true, "created"))
      )
      await(call(controller.transientPushNotification(transientPushProviderId), FakeRequest().withJsonBody(transientPushBody)))

      val captor = ArgumentCaptor.forClass(classOf[PushNotification])
      verify(messagingService).processTransientPushNotification(any(), captor.capture)(any())

      val push: PushNotification = captor.getValue

      push.ttl.get.toMinutes mustBe 4
      push.priority.get mustBe Priority.HIGH
      push.channel.get mustBe "two_step_codes"
    }

    "process transient push message" in {
      when(messagingService.processTransientPushNotification(any[Set[Usercode]], any[PushNotification])(any[AuditLogContext])).thenReturn(
        Future.successful(ProcessingResult(success = true, "created"))
      )
      val result = call(controller.transientPushNotification(transientPushProviderId), FakeRequest().withJsonBody(body))

      status(result) mustBe CREATED

      val json = contentAsJson(result)
      (json \ "success").as[Boolean] mustBe true
      (json \ "status").as[String] mustBe "ok"
    }

    "return forbidden when a provider does not have transient push permission" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      when(publisherService.getRoleForUser(tabulaPublisherId, ron.usercode)).thenReturn(APINotificationsManager)
      when(activityService.getProvider(tabula)).thenReturn(Some(tabulaProvider))

      val result = call(controller.transientPushNotification(tabula), FakeRequest().withJsonBody(body))
      status(result) mustBe FORBIDDEN

      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe false
      (json \ "status").as[String] mustBe "forbidden"
      (json \ "errors" \\ "message").map(_.as[String]).head must include("does not have permission")
    }
  }

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
      when(publisherService.getRoleForUser(isEq(tabulaPublisherId), any())).thenReturn(APINotificationsManager)
      when(activityService.save(any(), any())).thenReturn(Right("created-activity-id"))

      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(body))

      status(result) mustBe CREATED
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "status").as[String] mustBe "ok"
      (json \ "data" \ "id").as[String] mustBe "created-activity-id"
    }

    "send to a webgroup" in {
      val groupAudience = Audience(Seq(Audience.WebGroupAudience(GroupName("in-trigue"))))

      when(publisherService.getRoleForUser(isEq(tabulaPublisherId), any())).thenReturn(APINotificationsManager)
      when(activityService.save(any(), isEq(groupAudience))).thenReturn(Right("created-activity-id"))

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

    "have warnings attached to the success response if some usercodes are invalid" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      when(publisherService.getRoleForUser(any(), any())).thenReturn(APINotificationsManager)
      when(audienceService.resolve(any())).thenReturn(Try(Set(Usercode("u123123"), Usercode("u2938484"))))
      when(activityService.save(any(), any())).thenReturn(Right("created-activity-id"))
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(Json.obj(
        "type" -> "due",
        "title" -> "Coursework due soon",
        "url" -> "http://tabula.warwick.ac.uk",
        "text" -> "Your submission for CS118 is due tomorrow",
        "recipients" -> Json.obj(
          "users" -> Json.arr(
            "u123123",
            "u2938484",
            "invalid,sd",
          )
        )
      )))

      status(result) mustBe CREATED
      val json = contentAsJson(result)
      (json \ "warnings").isDefined mustBe true
      (json \ "warnings" \ 0 \ "message").as[String] mustBe "The request contains one or more invalid usercode: List(Usercode(invalid,sd))"
    }


    "bad request response if all usercodes are invalid" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      when(publisherService.getRoleForUser(any(), any())).thenReturn(APINotificationsManager)
      when(activityService.save(any(), any())).thenReturn(Right("created-activity-id"))
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(Json.obj(
        "type" -> "due",
        "title" -> "Coursework due soon",
        "url" -> "http://tabula.warwick.ac.uk",
        "text" -> "Your submission for CS118 is due tomorrow",
        "recipients" -> Json.obj(
          "users" -> Json.arr(
            "u12, 3123",
            "u,2938484",
            "invalid,sd",
          )
        )
      )))

      status(result) mustBe BAD_REQUEST
      val json = contentAsJson(result)
      (json \ "warnings").isEmpty mustBe true
      (json \ "errors" \ 0 \ "message").as[String] mustBe "All usercodes from this request seem to be invalid"
    }

    "happy with empty useraudiences" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      when(publisherService.getRoleForUser(any(), any())).thenReturn(APINotificationsManager)
      when(activityService.save(any(), any())).thenReturn(Right("created-activity-id"))
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(Json.obj(
        "type" -> "due",
        "title" -> "Coursework due soon",
        "url" -> "http://tabula.warwick.ac.uk",
        "text" -> "Your submission for CS118 is due tomorrow",
        "recipients" -> Json.obj(
          "users" -> Json.arr(),
          "groups" -> Json.arr(
            "in-trigue"
          )
        )
      )))
      status(result) mustBe CREATED
      val json = contentAsJson(result)
      (json \ "warnings").isEmpty mustBe true
    }

    "happy with all valid usercodes" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      when(publisherService.getRoleForUser(any(), any())).thenReturn(APINotificationsManager)
      when(activityService.save(any(), any())).thenReturn(Right("created-activity-id"))
      val result = call(controller.postNotification(tabula), FakeRequest().withJsonBody(Json.obj(
        "type" -> "due",
        "title" -> "Coursework due soon",
        "url" -> "http://tabula.warwick.ac.uk",
        "text" -> "Your submission for CS118 is due tomorrow",
        "recipients" -> Json.obj(
          "users" -> Json.arr(
            "sdf",
            "123sdf",
            "9jd@sdf.com"
          ),
        )
      )))

      status(result) mustBe CREATED
      val json = contentAsJson(result)
      (json \ "warnings").isEmpty mustBe true
    }

    "fail for too many recipients" in {
      when(publisherService.getParentPublisherId(tabula)).thenReturn(Some(tabulaPublisherId))
      when(publisherService.find(tabulaPublisherId)).thenReturn(Some(tabulaPublisher.copy(maxRecipients = Some(1))))
      when(audienceService.resolve(any())).thenReturn(Try(Set(Usercode("cusfal"), Usercode("cusebr"))))
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
