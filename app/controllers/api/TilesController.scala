package controllers.api

import com.google.inject.Inject
import models.UserTile
import play.api.libs.json._
import play.api.mvc.{Controller, Result}
import services.{SecurityService, TileContentService, TileService}
import warwick.sso.User

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
