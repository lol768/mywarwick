package controllers

import javax.inject.Singleton

import org.joda.time.DateTime
import play.api.mvc.{Controller, Action}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This is a bit of a jungle gym of experimentation as I work out a nice way of generating
 * an AppCache manifest. The manifest needs to change whenever any of the assets changes, otherwise
 * the browser will never grab a new version.
 */
@Singleton
class AppCacheController extends Controller {

  // Include this in the manifest so that it updates when we reload.
  // Last-compiled date would be better, but this is close enough for now.
  val startDate = new DateTime().toString

  val assets = Seq(
    "js/bundle.js",
    "css/main.css",
    "images/favicon.png",
    "id7/images/shim.gif",
    "id7/js/id7-bundle.min.js",
    "id7/images/masthead-logo-bleed-xs.svg"
  )


  def cacheManifest = Action.async { request =>

    // TODO can we avoid doing this on every request?
    // Currently assetInfo requires request (to get gzip info)
    val assetsWithInfo: Future[Seq[(String, String)]] = Future.sequence(assets.map { path =>
      val resourceName = Assets.resourceNameAt("/public", path)
      val getInfo: Future[Option[(AssetInfo, Boolean)]] = resourceName.map(controllers.Assets.assetInfoForRequest(request, _)).getOrElse {
        Future.failed(new IllegalArgumentException)
      }

      getInfo.map { opt =>
        val lastMod = opt.flatMap {
          case (info, gzip) => info.parsedLastModified.map(new DateTime(_).toString)
        }.getOrElse("-")
        (path -> lastMod)
      }
    })

    assetsWithInfo.map { metadata =>
      Ok(views.txt.cachemanifest(startDate, metadata)).withHeaders("Content-Type" -> "text/cache-manifest")
    }
  }
}
