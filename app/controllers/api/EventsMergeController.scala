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

  sealed abstract class Calendar(val value: String, val name: String)
  case object O365 extends Calendar("calendar", "Office 365 Calendar")
  case object UniEvents extends Calendar("uni-events", "University Events")
  case object Timetable extends Calendar("timetable", "Timetable")
  case object Sports extends Calendar("sports", "Sports")
  val allCalendars = Set(O365, Sports, Timetable, UniEvents)
  private val defaultCalendars = Set(O365, Timetable)

  private def buildPreferences(calendars: Set[Calendar]) =
    Json.obj(
      "calendars" -> Json.obj(
        "type" -> "array",
        "description" -> "Show events from these calendars",
        "options" -> calendars.map(cal =>
          Json.obj(
            "name" -> cal.name,
            "value" -> cal.value
          )
        ),
        "default" -> defaultCalendars.filter(calendars.contains(_)).map(_.value)
      )
    )

  private def preferenceListToSeq(value: JsValue): Set[String] =
    value.asOpt[Map[String, Boolean]]
      .map(_.flatMap {
        case (key, true) => Some(key)
        case _ => None
      })
      .getOrElse(Nil)
      .toSet

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
    val calendars: Set[String] = req.body.asJson.flatMap(json =>
      (json \ "calendars").asOpt[JsValue].map(preferenceListToSeq)
    ).getOrElse(defaultCalendars.map(_.value))

    val user = req.context.user

    Future.sequence(
      tileService.getTilesForUser(user)
        .filter(instance => calendars.contains(instance.tile.id))
        .map(tileContentService.getTileContent(user.map(_.usercode), _))
    )
      .map(mergeJsonItems(_))
      .map { events =>
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

  def preferences: Action[AnyContent] = security.RequiredUserAction { implicit req =>
    val ids = tileService.getTilesForUser(req.context.user).map(_.tile.id)
    val filteredPreferences: JsObject = buildPreferences(allCalendars.filter(c => ids.contains(c.value)).toSet)
    Ok(filteredPreferences)
  }
}
