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

  def tiles = UserAction { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    Ok(Json.toJson(API.Success("ok", tileLayout)))
  }

  def content = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    tilesContent(request.context.user, tileLayout.tiles)
  }

  def tilesContent(user: Option[User], tiles: Seq[TileInstance]): Future[Result] = {
    val futures = tiles.map { tile =>
      tileContentService.getTileContent(user, tile).map(content => (tile, content))
    }

    Future.sequence(futures).map { result =>
      val tileResult = for ((tile, API.Success(_, content)) <- result) yield tile.tile.id -> Map("content" -> content)
      val errorResult = for ((tile, API.Failure(_, errors)) <- result) yield tile.tile.id -> Map("errors" -> Json.toJson(errors))

      Ok(Json.toJson(API.Success(data = (tileResult ++ errorResult).toMap)))
    }
  }

  def contentById(id: String) = RequiredUserAction.async { request =>
    request.context.user.map { user =>
      val tiles = tileService.getTilesByIds(user, Seq(id))

      tilesContent(Option(user), tiles)
    }.get // RequiredUserAction
  }
}

