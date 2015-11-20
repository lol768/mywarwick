package controllers

import com.google.inject.Inject
import models.{ActivityResponse, ActivityTag}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Controller
import services.{ActivityService, SecurityService}

class ActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService
) extends Controller {

  import securityService._

  implicit val writesActivityTag = new Writes[ActivityTag] {
    override def writes(o: ActivityTag): JsValue = Json.obj(
      "name" -> o.name,
      "value" -> o.value.internalValue,
      "display_value" -> o.value.displayValue
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
