package controllers.api

import com.google.inject.Inject
import models.UserTile
import play.api.libs.json._
import play.api.mvc.{Controller, Result}
import services.{SecurityService, TileContentService, TileService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TilesController @Inject()(
  securityService: SecurityService,
  tileService: TileService,
  tileContentService: TileContentService
) extends Controller {

  import securityService._

  def tiles = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)

    Future(
      Ok(Json.obj(
        "type" -> "tiles",
        "tiles" -> tileLayout.tiles
      ))
    )
  }

  def tilesById(ids: Seq[String]) = RequiredUserAction.async { request =>
    request.context.user.map { user =>
      val tiles = tileService.getTilesByIds(user.usercode, ids)

      getTileResult(tiles)
    }.get // RequiredUserAction
  }

  private def getTileResult(tiles: Seq[UserTile]): Future[Result] = {
    val futureContent = tiles.map(tileContentService.getTileContent)

    Future.sequence(futureContent).map { content =>
      val result: Seq[(UserTile, JsObject)] = tiles.zip(content)

      Ok(Json.obj(
        "type" -> "tiles",
        "tiles" -> result.map {
          case (t, c) => Json.obj(
            "tile" -> t,
            "content" -> c
          )
        }
      ))
    }
  }
}
