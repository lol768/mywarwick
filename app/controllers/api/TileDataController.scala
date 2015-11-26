package controllers.api

import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.{PathBindable, Controller}
import services.TileDataService
import warwick.sso.SSOClient

class TileDataController @Inject()(
  ssoClient: SSOClient,
  tileDataService: TileDataService
) extends Controller {

  def requestTileData = ssoClient.Lenient { request =>
    val tileData = tileDataService.getTileConfig(request.context.user)
    Ok(Json.obj(
      "type" -> "tiles",
      "tiles" -> tileData
    ))
  }

  def requestTileDataByIds(tileIds: Seq[String]) = ssoClient.Lenient { request =>
    val tileData = tileDataService.getTileDataByIds(request.context.user, tileIds)
    Ok(Json.obj(
      "type" -> "tiles",
      "tiles" -> tileData
    ))
  }


}
