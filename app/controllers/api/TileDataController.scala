package controllers.api

import com.google.inject.Inject
import models.UserTile
import play.api.libs.json._
import play.api.mvc.Controller
import services.{SecurityService, TileContentService, TileService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TileDataController @Inject()(
  securityService: SecurityService,
  tileService: TileService,
  tileContentService: TileContentService
) extends Controller {

  import securityService._

  def requestTiles = UserAction.async { request =>
    val tileLayout = tileService.getTilesForUser(request.context.user)
    val tileContent = tileLayout.tiles.map(tileContentService.getTileContent)

    Future.sequence(tileContent).map { tileContent =>
      val result: Seq[(UserTile, JsObject)] = tileLayout.tiles.zip(tileContent)

      Ok(Json.obj(
        "type" -> "tiles",
        "tiles" -> result.map {
          case (tile, content) => Json.obj(
            "tile" -> tile,
            "content" -> content
          )
        }
      ))
    }
  }

}
