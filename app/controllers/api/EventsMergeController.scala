package controllers.api

import java.time.ZonedDateTime
import java.time.temporal.Temporal

import com.google.inject.Inject
import controllers.MyController
import javax.inject.Named
import models.API
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent}
import services.{SecurityService, TileContentService, TileService}
import uk.ac.warwick.util.core.DateTimeUtils
import uk.ac.warwick.util.termdates.AcademicYear

import scala.concurrent.{ExecutionContext, Future}

class EventsMergeController @Inject()(
  tileService: TileService,
  tileContentService: TileContentService,
  security: SecurityService
)(implicit @Named("tileData") ec: ExecutionContext) extends MyController {

  object EventsMergeController {
    val defaultCalendars = Seq(
      "calendar",
      "timetable"
    )

    val preferences = Json.obj(
      "calendars" -> Json.obj(
        "type" -> "array",
        "description" -> "Show events from these calendars",
        "options" -> Json.arr(
          Json.obj(
            "name" -> "calendar",
            "value" -> "Office 365 Calendar"
          ),
          Json.obj(
            "name" -> "timetable",
            "value" -> "Timetable"
          ),
          Json.obj(
            "name" -> "sports",
            "value" -> "Sports"
          ),
          Json.obj(
            "name" -> "uni-events",
            "value" -> "University Events"
          ),
        ),
        "default" -> defaultCalendars
      )
    )
  }

  private def preferenceListToSeq(value: JsValue): Seq[String] =
    value.asOpt[Map[String, Boolean]]
      .map(_.flatMap {
        case (key, true) => Some(key)
        case _ => None
      })
      .getOrElse(Nil)
      .toSeq

  private def mergeJsonItems(responses: Seq[API.Response[JsObject]]): Seq[JsValue] = {
    val items = responses.map {
      case API.Success(_, jsObj) => (jsObj \ "items").as[JsArray]
      case _ => Json.arr()
    }
    if (items.nonEmpty)
      items.reduce(_ ++ _).value.sortBy(e => (e \ "start").as[String])
    else
      items
  }

  def index: Action[AnyContent] = security.RequiredUserAction.async { implicit req =>
    val calendars: Seq[String] = req.body.asJson.flatMap(json =>
      (json \ "calendars").asOpt[JsValue].map(preferenceListToSeq)
    ).getOrElse(EventsMergeController.defaultCalendars)

    val user = req.context.user

    Future.sequence(
      tileService.getTilesForUser(user)
        .filter(instance => calendars.contains(instance.tile.id))
        .map(tileContentService.getTileContent(user.map(_.usercode), _))
    )
      .map(mergeJsonItems(_))
      .map { events => {
        val temporal: Temporal = ZonedDateTime.now(DateTimeUtils.CLOCK_IMPLEMENTATION)
        val academicWeek = AcademicYear.forDate(temporal).getAcademicWeek(temporal).getWeekNumber
        Ok(Json.toJson(API.Success(
          data = Json.obj(
            "defaultText" -> s"You have no events",
            "items" -> events,
            "currentWeek" -> academicWeek
          )
        )))
      }
      }
  }

  def preferences: Action[AnyContent] = Action {
    Ok(EventsMergeController.preferences)
  }
}
