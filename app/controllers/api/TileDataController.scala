package controllers.api

import com.google.inject.Inject
import play.api.libs.json._
import play.api.mvc.Controller
import services.TileService
import warwick.sso.SSOClient

class TileDataController @Inject()(
  ssoClient: SSOClient,
  tileService: TileService
) extends Controller {

  def requestTiles = ssoClient.Lenient { request =>
    val tilesConfig = tileService.getTilesConfig(request.context.user)
    val tileData = tileService.getTilesData(tilesConfig)

    val tiles = JsObject(Seq(
      "tileConfig" -> Json.toJson(tilesConfig),
      "tileData" -> Json.toJson(tileData)
    ))

    Ok(Json.obj(
      "type" -> "tiles",
      "tiles" -> tiles
    ))
  }

}
