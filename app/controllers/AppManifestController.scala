package controllers

import com.google.inject.Inject
import play.api.Configuration
import play.api.libs.json.{JsString, Json}
import services.SecurityService

class AppManifestController @Inject()(
  securityService: SecurityService,
  configuration: Configuration
) extends BaseController {

  import securityService._

  def getAppManifest = UserAction { implicit request =>
    Ok(Json.obj(
      "name" -> JsString("Start.Warwick"),
      "short_name" -> JsString("Start.Warwick"),
      "start_url" -> JsString("/"),
      "display" -> JsString("standalone"),
      "icons" -> Json.arr(
        Json.obj(
          "src" -> JsString("/assets/lib/id7/images/android-chrome-144x144.png"),
          "sizes" -> JsString("144x144"),
          "type" -> JsString("image/png")
        ),
        Json.obj(
          "src" -> JsString("/assets/lib/id7/images/android-chrome-192x192.png"),
          "sizes" -> JsString("192x192"),
          "type" -> JsString("image/png"
          )
        )
      ),
      "gcm_sender_id" -> JsString(configuration.getString("start.gcm.id").getOrElse(throw new IllegalStateException("Missing GCM Sender Id - set start.gcm.id")))
    ))
  }
}
