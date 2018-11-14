package controllers

import controllers.Assets.Asset
import javax.inject.{Inject, Singleton}
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

@Singleton
class AssetsController @Inject() (
  assets: Assets
)( implicit
  executionContext: ExecutionContext
) extends InjectedController {

  /**
    * Delegates assets to the regular Play Assets, but if the requested file
    * contained a fingerprint and it's a 404, try again without the fingerprint. NEWSTART-1609
    */
  def asset(basePath: String, asset: Asset) = Action.async { request =>
    val future = assets.versioned(basePath, asset)(request)
    removeFingerprint(asset).map { unversioned =>
      future.flatMap { result =>
        if (result.header.status == NOT_FOUND)
          assets.versioned(basePath, unversioned)(request).map { result =>
            result.withHeaders(
              WARNING -> "Outdated fingerprinted asset, trying unfingerprinted version."
            )
          }
        else
          future
      }
    } getOrElse {
      future
    }
  }

  /**
    * Asset("/assets/css/63cd4e8316e4f01fdb1cc9ef2c85e500-admin.css")
    *   returns
    * Some(Asset("/assets/css/admin.css"))
    *   anything without a fingerprint detected returns
    * None
    */
  def removeFingerprint(asset: Asset): Option[Asset] = {
    val regex = "^(.*/)[0-9a-f]{16,}-([^/]+)$".r
    val matcher = regex.pattern.matcher(asset.name)
    if (matcher.matches) {
      val name = s"${matcher.group(1)}${matcher.group(2)}"
      Some(asset.copy(name = name))
    } else {
      None
    }
  }


}
