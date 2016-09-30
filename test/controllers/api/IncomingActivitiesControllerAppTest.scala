package controllers.api

import helpers.{Fixtures, OneStartAppPerSuite, TestApplications}
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import warwick.sso._

class IncomingActivitiesControllerAppTest extends PlaySpec with Results with OneStartAppPerSuite {

  lazy val ron = Users.create(usercode = Usercode("ron"))

  implicit override lazy val app = TestApplications.full(Some(ron))

  get[Database].withConnection { implicit c =>
    Fixtures.sql.insertPublisherPermission(publisherId = "elab", usercode = "ron", role = "APINotificationsManager").execute()
    Fixtures.sql.insertActivityType(name = "due", displayName = "Coursework due").execute()
  }

  val controller = get[IncomingActivitiesController]

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
