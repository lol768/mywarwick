package controllers.api

import com.google.inject.Inject
import models.{API, UserTile}
import play.api.libs.json._
import play.api.mvc.{Controller, Result}
import services.{SecurityService, TileContentService, TileService}
import warwick.sso.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class TileAndContent(tile: UserTile, content: JsObject)
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
) extends Controller {

  import securityService._

  def tiles = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)

    getTileResult(request.context.user, tileLayout.tiles)
  }

  def tilesById(ids: Seq[String]) = RequiredUserAction.async { request =>
    request.context.user.map { user =>
      val tiles = tileService.getTilesByIds(user.usercode, ids)

      getTileResult(Option(user), tiles)
    }.get // RequiredUserAction
  }

  private def getTileResult(user: Option[User], tiles: Seq[UserTile]): Future[Result] = {
    val futures = tiles.map { t =>
      tileContentService.getTileContent(user, t).map((t, _))
    }

    Future.sequence(futures).map { result =>
      val tileResult = TileResult(result.map {
        case(tile, content) => TileAndContent(tile, content)
      })
      Ok(Json.toJson(API.Success[TileResult]("ok", tileResult)))
    }
  }
}

