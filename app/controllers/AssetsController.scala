package controllers

import controllers.Assets.Asset
import javax.inject.{Inject, Singleton}
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

/**
  * Handles assets by delegating to the regular Assets controller, but deals
  * with requests for old file hashes by serving whatever version we have.
  */
@Singleton
class AssetsController @Inject() (
  assets: Assets
)(implicit
  executionContext: ExecutionContext
) extends InjectedController {

  /**
    * Delegates assets to the regular Play Assets, but if the requested file
    * contained a fingerprint and it's a 404, try again without the fingerprint. NEWSTART-1609
    */
  def versioned(basePath: String, asset: Asset) = Action.async { request =>
    val future = assets.versioned(basePath, asset)(request)
    AssetsController.removeFingerprint(asset).map { unversioned =>
      future.flatMap { result =>
        if (result.header.status == NOT_FOUND)
          assets.versioned(basePath, unversioned)(request).map { result =>
            // this could still be a 404 if it's a nonsense filename, but add the Warning regardless
            result.withHeaders(
              WARNING -> """299 MyWarwick "Outdated fingerprinted asset, trying unfingerprinted version.""""
            )
          }
        else
          future
      }
    } getOrElse {
      future
    }
  }


}

object AssetsController {

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
