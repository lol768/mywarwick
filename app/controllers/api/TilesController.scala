package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import models._
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.mvc.Result
import services.{SecurityService, TileContentService, TileService}
import warwick.sso.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class SaveTilesRequest(
  tiles: Seq[UserTileSetting],
  layout: Seq[TileLayout]
)

object SaveTilesRequest {
  implicit val format = Json.format[SaveTilesRequest]
}

@Singleton
class TilesController @Inject()(
  securityService: SecurityService,
  tileService: TileService,
  tileContentService: TileContentService
) extends MyController {

  import securityService._

  def getLayout = UserAction.async { request =>
    val user = request.context.user
    val tiles = tileService.getTilesForUser(user)
    val layout = tileService.getTileLayoutForUser(user)
    val optionsFuture = tileContentService.getTilesOptions(tiles.map(_.tile))

    for {
      options <- optionsFuture
    } yield {
      Ok(Json.toJson(API.Success("ok", Json.obj(
        "tiles" -> tiles,
        "layout" -> layout,
        "options" -> options
      )))).as(withCharset(JSON))
    }
  }

  def saveLayout = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      request.body.asJson.map { body =>
        body.validate[SaveTilesRequest] match {
          case JsSuccess(tileLayout, _) =>
            tileService.saveTilePreferencesForUser(user, tileLayout.tiles)
            tileService.saveTileLayoutForUser(user, tileLayout.layout)
            Ok(Json.toJson(API.Success("ok", "saved")))
          case error: JsError =>
            BadRequest(Json.toJson(API.Failure[JsObject]("error", API.Error.fromJsError(error))))
        }
      }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted tile layout"))))))
    }.get // RequiredUserAction
  }

  def content = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    tilesContent(request.context.user, tileLayout)
  }

  def tilesContent(user: Option[User], tiles: Seq[TileInstance]): Future[Result] = {
    val futures = tiles.filter(_.tile.fetchUrl.isDefined).map { tile =>
      tileContentService.getTileContent(user.map(_.usercode), tile).map(content => (tile, content))
    }

    Future.sequence(futures).map { result =>
      val tileResult = for ((tile, API.Success(_, content)) <- result) yield tile.tile.id -> Map("content" -> content)
      val errorResult = for ((tile, API.Failure(_, errors)) <- result) yield tile.tile.id -> Map("errors" -> Json.toJson(errors))

      Ok(Json.toJson(API.Success(data = (tileResult ++ errorResult).toMap))).as(withCharset(JSON))
    }
  }

  def contentById(id: String) = UserAction.async { request =>
    val tiles = tileService.getTilesByIds(request.context.user, Seq(id))
    tiles match {
      case Seq(_) => tilesContent(request.context.user, tiles)
      case Seq() => Future(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("no-tile", s"Cannot fetch content. No tiles exist with id '$id'"))))))
    }
  }
}

