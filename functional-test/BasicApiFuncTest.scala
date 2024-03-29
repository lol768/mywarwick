import models.API
import play.api.http.Status
import play.api.libs.json._
import helpers.ApiFuncTestBase
import org.scalatestplus.play.PortNumber

/**
  * ApiFuncTestBase currently doesn't work.
  */
class BasicApiFuncTest extends ApiFuncTestBase {
  implicit def portNumber: PortNumber = ???

  "A generic user agent" ignore {
    "be able to access the Web App Manifest" in {
      val manifest = wsUrl("/assets/manifest.json").get.futureValue
      manifest.status should be(Status.OK)
      (manifest.json \ "name").get should be(JsString("My Warwick"))
      (manifest.json \ "display").get should be(JsString("standalone"))
    }

    "be able to access the script bundle" in {
      val bundle = wsUrl("/assets/js/bundle.js").get.futureValue
      bundle.status should be (Status.OK)
    }
  }

  "The API" ignore {
    "signed out" should {
      "return the anonymous tile set" in {
        val testHeronTile = Json.obj(
          "id" -> "heron-tile",
          "colour" -> 3,
          "icon" -> "envelope-o",
          "preferences" -> JsNull,
          "title" -> "Mail",
          "type" -> "count",
          "removed" -> false
        )

        val activities = wsCall(controllers.api.routes.TilesController.getLayout).get.futureValue
        val response = activities.json.as[API.Response[JsValue]]
        response should be(API.Success("ok", Json.obj(
          "tiles" -> Json.arr(testHeronTile),
          "layout" -> Json.arr()
        )))
      }
    }
  }
}
