package controllers.api

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{SecurityService, TileContentService, TileService, TimetableTokenService}
import warwick.sso.UserLookupService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TimetablesController @Inject()(
  securityService: SecurityService,
  timetableTokenService: TimetableTokenService,
  tileContentService: TileContentService,
  tileService: TileService,
  userLookupService: UserLookupService
) extends BaseController {

  import securityService._

  def register: Action[AnyContent] = APIAction { request =>
    val usercode = request.context.user.get.usercode

    val token = timetableTokenService.create(usercode)

    Ok(Json.obj(
      "success" -> true,
      "status" -> "ok",
      "token" -> token.string
    ))
  }

  def get: Action[AnyContent] = Action.async { request =>
    request.headers.get("X-Timetable-Token")
      .flatMap(timetableTokenService.validate)
      .map { usercode =>
        val user = userLookupService.getUser(usercode).toOption
        val Seq(tileInstance) = tileService.getTilesByIds(user, Seq("timetable"))
        tileContentService.getTileContent(Some(usercode), tileInstance)
          .map(r => Ok(Json.toJson(r)))
      }.getOrElse {
      Future.successful(Unauthorized(Json.obj(
        "success" -> false,
        "status" -> "unauthorized"
      )))
    }
  }

}
