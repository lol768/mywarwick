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
) extends MyController {

  val senderId = configuration.get[String]("mywarwick.fcm.id")

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
            "src" -> "/assets/images/icon-48.png",
            "sizes" -> "48x48",
            "type" -> "image/png"
          ),Json.obj(
            "src" -> "/assets/images/icon-72.png",
            "sizes" -> "72x72",
            "type" -> "image/png"
          ),Json.obj(
            "src" -> "/assets/images/icon-76.png",
            "sizes" -> "76x76",
            "type" -> "image/png"
          ),Json.obj(
            "src" -> "/assets/images/icon-96.png",
            "sizes" -> "96x96",
            "type" -> "image/png"
          ),Json.obj(
            "src" -> "/assets/images/icon-120.png",
            "sizes" -> "120x120",
            "type" -> "image/png"
          ),Json.obj(
            "src" -> "/assets/images/icon-144.png",
            "sizes" -> "144x144",
            "type" -> "image/png"
          ),Json.obj(
            "src" -> "/assets/images/icon-152.png",
            "sizes" -> "152x152",
            "type" -> "image/png"
          ),
          Json.obj(
            "src" -> "/assets/images/icon-180.png",
            "sizes" -> "180x180",
            "type" -> "image/png"
          ),
          Json.obj(
            "src" -> "/assets/images/icon-192.png",
            "sizes" -> "192x192",
            "type" -> "image/png"
          ),
          Json.obj(
            "src" -> "/assets/images/icon-512.png",
            "sizes" -> "512x512",
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
