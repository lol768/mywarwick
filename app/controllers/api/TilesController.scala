package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import models.{API, TileInstance}
import play.api.libs.json._
import play.api.mvc.Result
import services.{SecurityService, TileContentService, TileService}
import warwick.sso.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TilesController @Inject()(
  securityService: SecurityService,
  tileService: TileService,
  tileContentService: TileContentService
) extends BaseController {

  import securityService._

  def tilesConfig = UserAction { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    Ok(Json.toJson(API.Success("ok", tileLayout)))
  }

  def allTilesContent = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    tilesContent(request.context.user, tileLayout.tiles)
  }

  def tilesContent(user: Option[User], tiles: Seq[TileInstance]): Future[Result] = {
    val futures = tiles.map { tile =>
      tileContentService.getTileContent(user, tile).map(content => (tile, content))
    }

    Future.sequence(futures).map { result =>
      val tileResult = for ((tile, API.Success(_, content)) <- result) yield tile.tile.id -> content
      val errorResult = for ((tile, API.Failure(_, errors)) <- result) yield tile.tile.id -> Json.toJson(errors)

      if (tileResult.isEmpty) {
        InternalServerError(Json.obj(
          "success" -> false,
          "status" -> "Internal Server Error",
          "errors" -> JsObject(errorResult)
        ))
      } else {
        val allOk = tileResult.length == futures.length

        val (status, response) = if (allOk) {
          ("ok", Json.obj("tiles" -> JsObject(tileResult)))
        } else {
          ("some-ok", Json.obj("tiles" -> JsObject(tileResult), "errors" -> JsObject(errorResult)))
        }

        Ok(Json.toJson(API.Success(status, response)))
      }
    }
  }

  def tilesContentById(ids: Seq[String]) = RequiredUserAction.async { request =>
    request.context.user.map { user =>
      val tiles = tileService.getTilesByIds(user, ids)

      tilesContent(Option(user), tiles)
    }.get // RequiredUserAction
  }
}

