package controllers.api

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import anorm._
import helpers.Fixtures.mockLoginContext
import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.db.Database
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services._
import warwick.sso._

class IncomingActivitiesControllerAppTest extends PlaySpec with Results with OneStartAppPerSuite {

  implicit val akka = get[ActorSystem]
  implicit val mat = ActorMaterializer()

  val ron = Users.create(usercode = Usercode("ron"))

  val ssoClient = new MockSSOClient(mockLoginContext(Some(ron)))

  get[Database].withConnection { implicit c =>
    SQL"INSERT INTO PUBLISHER_PERMISSION (PUBLISHER_ID, USERCODE, ROLE) VALUES ('elab', 'ron', 'APINotificationsManager')"
      .execute()
    SQL"INSERT INTO ACTIVITY_TYPE (NAME, DISPLAY_NAME) VALUES ('due', 'Coursework due')"
      .execute()
  }

  val controller = new IncomingActivitiesController(
    new SecurityServiceImpl(ssoClient, get[BasicAuth], get[CacheApi]),
    get[ActivityService],
    get[ActivityRecipientService],
    get[PublisherService],
    get[MessagesApi]
  )

  "IncomingActivitiesController" should {

    "create a notification" in {
      val body = Json.obj(
        "type" -> "due",
        "title" -> "Coursework due soon",
        "url" -> "http://tabula.warwick.ac.uk",
        "text" -> "Your submission for CS118 is due tomorrow",
        "recipients" -> Json.obj(
          "users" -> Json.arr("u1473579")
        )
      )

      val result = call(controller.postNotification("tabula"), FakeRequest().withBody(body))

      status(result) must be(CREATED)
      val json = contentAsJson(result)

      (json \ "success").as[Boolean] must be(true)
      (json \ "status").as[String] must be("ok")
      (json \ "data" \ "id").as[String] must have length 36
    }

  }

}
