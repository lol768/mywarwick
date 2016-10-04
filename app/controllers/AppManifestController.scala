package controllers

import javax.inject.Singleton

import com.google.inject.Inject
import play.api.Configuration
import play.api.cache.Cached
import play.api.libs.json.Json
import play.api.mvc.Action

@Singleton
class AppManifestController @Inject()(
  configuration: Configuration,
  cached: Cached
) extends BaseController {

  val gcmSenderId = configuration.getString("mywarwick.gcm.id")
    .getOrElse(throw new IllegalStateException("Missing GCM Sender Id - set mywarwick.gcm.id"))

  def getAppManifest = cached("manifest") {
    Action {
      Ok(Json.obj(
        "name" -> "My Warwick",
        "short_name" -> "My Warwick",
        "start_url" -> "/",
        "display" -> "standalone",
        "background_color" -> "#8C6E96",
        "theme_color" -> "#8C6E96",
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
