package controllers.api

import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.Controller
import services.TileDataService
import warwick.sso.SSOClient

class TileDataController @Inject()(
  ssoClient: SSOClient,
  tileDataService: TileDataService
) extends Controller {

  def requestTileData = ssoClient.Lenient { request =>
    val tileData = tileDataService.getTileData(request.context.user)

    Ok(Json.obj(
      "type" -> "tiles",
      "tiles" -> tileData
    ))
  }

}
