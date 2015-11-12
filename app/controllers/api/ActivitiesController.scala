package controllers.api

import com.google.inject.Inject
import models.PostedActivity
import play.api.libs.json._
import play.api.mvc.Controller
import services.{ActivityService, SecurityService}

class ActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService
) extends Controller {

  import securityService._

  case class ActivitiesPostBody(
    notifications: Option[Seq[PostedActivity]],
    activities: Option[Seq[PostedActivity]]
  )

  implicit val dateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  implicit val postedActivityReads = Json.reads[PostedActivity]
  implicit val activityPostBodyReads = Json.reads[ActivitiesPostBody]

  def post(appId: String) = APIAction(parse.json) { request =>
    request.context.user.map { user =>
      request.body.validate[ActivitiesPostBody].map { data =>
        val activityIds = saveActivities(data.activities, appId, shouldNotify = false)
        val notificationIds = saveActivities(data.notifications, appId, shouldNotify = true)

        Created(Json.obj(
          "status" -> "ok",
          "activities" -> activityIds,
          "notifications" -> notificationIds
        ))
      }.recoverTotal {
        e => BadRequest(JsError.toJson(e))
      }
    }.get // APIAction calls this only if request.context.user is defined
  }

  private def saveActivities(postedActivities: Option[Seq[PostedActivity]], appId: String, shouldNotify: Boolean): Seq[String] = {
    postedActivities
      .getOrElse(Seq.empty)
      .map(_.toActivityPrototype(appId, shouldNotify))
      .map(activityService.save)
  }
}
