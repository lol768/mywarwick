package controllers

import com.google.inject.Inject
import play.api.Configuration
import play.api.cache.Cached
import play.api.libs.json.Json
import play.api.mvc.Action

class AppManifestController @Inject()(
  configuration: Configuration,
  cached: Cached
) extends BaseController {

  val gcmSenderId = configuration.getString("start.gcm.id")
    .getOrElse(throw new IllegalStateException("Missing GCM Sender Id - set start.gcm.id"))

  def getAppManifest = cached("manifest") {
    Action {
      Ok(Json.obj(
        "name" -> "Start.Warwick",
        "short_name" -> "Start.Warwick",
        "start_url" -> "/",
        "display" -> "standalone",
        "icons" -> Json.arr(
          Json.obj(
            "src" -> "/assets/lib/id7/images/android-chrome-144x144.png",
            "sizes" -> "144x144",
            "type" -> "image/png"
          ),
          Json.obj(
            "src" -> "/assets/lib/id7/images/android-chrome-192x192.png",
            "sizes" -> "192x192",
            "type" -> "image/png"
          )
        ),
        "gcm_sender_id" -> gcmSenderId
      ))
    }
  }
}
