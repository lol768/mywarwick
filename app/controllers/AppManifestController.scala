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

  val senderId = configuration.getString("mywarwick.fcm.id")
    .getOrElse(throw new IllegalStateException("Missing FCM Sender ID - set mywarwick.fcm.id"))

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
            "src" -> "/assets/images/icon-144.png",
            "sizes" -> "144x144",
            "type" -> "image/png"
          ),
          Json.obj(
            "src" -> "/assets/images/icon-192.png",
            "sizes" -> "192x192",
            "type" -> "image/png"
          )
        ),
        "gcm_sender_id" -> senderId,
        "prefer_related_applications" -> true,
        "related_applications" -> Json.arr(
          Json.obj(
            "platform" -> "play",
            "id" -> "uk.ac.warwick.my.app"
          ),
          Json.obj(
            "platform" -> "itunes",
            "url" -> "https://itunes.apple.com/gb/app/my-warwick/id1162088811?mt=8"
          )
        )
      ))
    }
  }
}
