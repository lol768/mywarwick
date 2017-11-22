package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import models.API
import play.api.libs.json._
import services.{ActivityService, SecurityService, UserPreferencesService}

@Singleton
class UserStreamFilterController @Inject()(
  security: SecurityService,
  userPreferencesService: UserPreferencesService,
  activityService: ActivityService
) extends MyController {

  import security._

  private def filterOptions: JsObject = {
    Json.obj(
      "provider" -> activityService.allProviders.map { case (provider, icon) => Json.obj(
        "id" -> provider.id,
        "displayName" -> provider.displayName.orNull[String],
        "icon" -> icon.map(_.name).orNull[String],
        "colour" -> icon.flatMap(_.colour).orNull[String]
      )}
    )
  }

  def notificationFilter = RequiredUserAction { request =>
    val user = request.context.user.get // RequiredUserAction

    Ok(Json.toJson(API.Success(data = Json.obj(
      "filter" -> userPreferencesService.getNotificationFilter(user.usercode),
      "options" -> filterOptions
    ))))
  }

  def activityFilter = RequiredUserAction { request =>
    val user = request.context.user.get // RequiredUserAction

    Ok(Json.toJson(API.Success(data = Json.obj(
      "filter" -> userPreferencesService.getActivityFilter(user.usercode),
      "options" -> filterOptions
    ))))
  }

  def updateNotificationFilter() = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    request.body.asJson.collect { case o: JsObject => o }.map { jsObject =>
      userPreferencesService.setNotificationFilter(user.usercode, jsObject)
      Ok(Json.toJson(API.Success("ok", "saved")))
    }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted"))))))
  }

  def updateActivityFilter() = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    request.body.asJson.collect { case o: JsObject => o }.map { jsObject =>
      userPreferencesService.setActivityFilter(user.usercode, jsObject)
      Ok(Json.toJson(API.Success("ok", "saved")))
    }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted"))))))
  }
}
