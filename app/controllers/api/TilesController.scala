package controllers.api

import java.io.IOException

import com.google.inject.Inject
import controllers.BaseController
import models.{API, TileInstance, TileLayout}
import play.api.libs.json
import play.api.libs.json._
import play.api.mvc.Result
import services.{SecurityService, TileContentService, TileService}
import warwick.sso.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class TileAndContent(tile: TileInstance, content: JsObject)

object TileAndContent {
  implicit val format = Json.format[TileAndContent]
}

case class TileResult(tiles: Seq[TileAndContent])

object TileResult {
  implicit val format = Json.format[TileResult]
}


class TilesController @Inject()(
  securityService: SecurityService,
  tileService: TileService,
  tileContentService: TileContentService
) extends BaseController {

  import securityService._

  def tilesConfig = UserAction { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    Ok(Json.toJson(API.Success[TileLayout]("ok", tileLayout)))
  }

  def allTilesContent = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    tilesContent(request.context.user, tileLayout.tiles)
  }

  def tilesContent(user: Option[User], tiles: Seq[TileInstance]): Future[Result] = {

    val futures = tiles.map { t =>
      tileContentService.getTileContent(user, t).map {
        // replicates current behaviour of aborting the whole thing.
        // TODO return tiles that worked, don't bail out completely
        case s: API.Success[JsObject] => s.data
        case API.Failure(status, errors) =>
          throw new IOException(errors.map(_.message).mkString("; "))
      }.map((t, _))
    }

    Future.sequence(futures).map { result =>
      val tileResult = result.map {
        case (tile, content) =>
          tile.tile.id -> content
      }
      Ok(Json.toJson(API.Success[JsObject]("ok", JsObject(tileResult))))
    }
  }

  def tilesContentById(ids: Seq[String]) = RequiredUserAction.async { request =>
    request.context.user.map { user =>
      val tiles = tileService.getTilesByIds(user, ids)

      tilesContent(Option(user), tiles)
    }.get // RequiredUserAction
  }
}

