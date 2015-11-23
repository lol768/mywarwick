package controllers

import com.google.inject.Inject
import models.{ActivityResponse, ActivityTag}
import play.api.libs.json.{JsString, JsValue, Json, Writes}
import play.api.mvc.Controller
import services.{ActivityService, SecurityService}

class ActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService
) extends Controller {

  import securityService._

  implicit val writesActivityTag = new Writes[ActivityTag] {
    override def writes(tag: ActivityTag): JsValue = Json.obj(
      "name" -> tag.name,
      "value" -> tag.value.internalValue,
      "display_value" -> JsString(tag.value.displayValue.getOrElse(tag.value.internalValue))
    )
  }

  implicit val writesActivityResponse = Json.writes[ActivityResponse]

  def get = RequiredUserAction { implicit request =>

    val activities = request.context.user
      .map(activityService.getActivitiesForUser)
      .getOrElse(Seq.empty)

    Ok(Json.obj(
      "success" -> true,
      "status" -> "ok",
      "activities" -> activities
    ))

  }

}
