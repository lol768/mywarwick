package controllers.api

import java.io.IOException

import com.google.inject.Inject
import controllers.BaseController
import models.API.Response
import models.{API, TileInstance}
import play.api.libs.json._
import play.api.mvc.{Controller, Result}
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

  def tiles = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)

    getTileResult(request.context.user, tileLayout.tiles)
  }

  def tilesById(ids: Seq[String]) = RequiredUserAction.async { request =>
    request.context.user.map { user =>
      val tiles = tileService.getTilesByIds(user, ids)

      getTileResult(Option(user), tiles)
    }.get // RequiredUserAction
  }

  private def getTileResult(user: Option[User], tiles: Seq[TileInstance]): Future[Result] = {
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
      val tileResult = TileResult(result.map {
        case(tile, content) => TileAndContent(tile, content)
      })
      Ok(Json.toJson(API.Success[TileResult]("ok", tileResult)))
    }
  }
}

