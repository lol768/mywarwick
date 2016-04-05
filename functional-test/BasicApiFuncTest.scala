import models.API
import play.api.http.Status
import play.api.libs.json._
import helpers.ApiFuncTestBase


class BasicApiFuncTest extends ApiFuncTestBase {
  "A generic user agent" - {
    "should be able to access the Web App Manifest" in {
      val manifest = wsUrl("/assets/manifest.json").get.futureValue
      manifest.status should be(Status.OK)
      (manifest.json \ "name").get should be(JsString("Start.Warwick"))
      (manifest.json \ "display").get should be(JsString("standalone"))
    }

    "should be able to access the script bundle" in {
      val bundle = wsUrl("/assets/js/bundle.js").get.futureValue
      bundle.status should be (Status.OK)
    }
  }

  "The API" - {
    "when signed out" - {
      "should return the anonymous tile set" in {
        val testHeronTile = Json.obj(
          "id" -> "heron-tile",
          "colour" -> 3,
          "defaultSize" -> "small",
          "icon" -> "envelope-o",
          "preferences" -> JsNull,
          "size" -> "small",
          "title" -> "Mail",
          "type" -> "count",
          "removed" -> false
        )

        val activities = wsCall(controllers.api.routes.TilesController.getLayout).get.futureValue
        val response = activities.json.as[API.Response[JsValue]]
        response should be(API.Success("ok", Json.arr(testHeronTile)))
      }
    }
  }
}
