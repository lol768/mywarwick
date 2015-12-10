package controllers.api

import com.google.inject.Inject
import models.TileInstance$
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

    getTileResult(tileLayout.tiles)
  }

  def tilesById(ids: Seq[String]) = RequiredUserAction.async { request =>
    request.context.user.map { user =>
      val tiles = tileService.getTilesByIds(user, ids)

      getTileResult(tiles)
    }.get // RequiredUserAction
  }

  private def getTileResult(tiles: Seq[TileInstance]): Future[Result] = {
    val futureContent = tiles.map(tileContentService.getTileContent)

    Future.sequence(futureContent).map { content =>
      val result: Seq[(TileInstance, JsObject)] = tiles.zip(content)

      Ok(Json.obj(
        "success" -> true,
        "status" -> "ok",
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
