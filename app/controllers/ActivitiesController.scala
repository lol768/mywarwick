package controllers

import com.google.inject.Inject
import models.{ActivityResponse, Activity}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.{ActivityService, SecurityService}

class ActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService
) extends Controller {

  import securityService._

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
