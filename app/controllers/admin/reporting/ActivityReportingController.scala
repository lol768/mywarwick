package controllers.admin.reporting

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models.ActivityProvider
import org.joda.time.{DateTime, Interval}
import play.api.data.Form
import play.api.data.Forms.{jodaDate, mapping}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, Result}
import services.SecurityService
import services.elasticsearch.ActivityDocument
import services.reporting.ActivityReportingService
import system.Roles
import warwick.sso.AuthenticatedRequest

import scala.collection.immutable.ListMap
import scala.concurrent.Future

@Singleton
class ActivityReportingController @Inject()(
  activityReportingService: ActivityReportingService,
  securityService: SecurityService,
  val messagesApi: MessagesApi
) extends BaseController with I18nSupport {

  import Roles._
  import securityService._
  import system.ThreadPools.elastic

  object FormField {
    val from = "fromDate"
    val to = "toDate"
  }

  val formData = Form(
    mapping(
      "fromDate" -> jodaDate("yyyy-MM-dd'T'HH:mm:ss"),
      "toDate" -> jodaDate("yyyy-MM-dd'T'HH:mm:ss")
    )(ActivityReportFormData.apply)(ActivityReportFormData.unapply) verifying(
      """"From" date must be before "To" date""",
      fields => fields match {
        case data => ActivityReportFormData.validate(data.fromDate, data.toDate).isDefined
      })
  )

  def index = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    val interval = ActivityReportingController.makeDefaultTimeDate()
    activityReportingService.allAlertsByProviders(interval).map(result => {
      Ok(views.html.admin.reporting.activity.index(
        result,
        formData.fill(ActivityReportFormData(interval.getStart, interval.getEnd))
      ))
    })
  }

  def formSubmit = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    formData.bindFromRequest.fold(
      formError => {
        val interval = ActivityReportingController.makeDefaultTimeDate()
        activityReportingService.allAlertsByProviders(interval).map(result => {
          Ok(views.html.admin.reporting.activity.index(
            result,
            formError.fill(ActivityReportFormData(interval.getStart, interval.getEnd))
          ))
        })
      },
      data => {
        val interval = new Interval(data.fromDate, data.toDate)
        activityReportingService.allAlertsByProviders(interval).map(result => {
          Ok(views.html.admin.reporting.activity.index(
            result,
            formData.fill(ActivityReportFormData(interval.getStart, interval.getEnd))
          ))
        })
      }
    )
  }
}

object ActivityReportingController {

  def makeDefaultTimeDate() = {
    val defaultTo = DateTime.now()
    val defaultFrom = defaultTo.minusDays(14)
    new Interval(defaultFrom, defaultTo)
  }
}

case class ActivityReportFormData(fromDate: DateTime, toDate: DateTime)

object ActivityReportFormData {
  def validate(fromDate: DateTime, toDate: DateTime) = {
    toDate.isAfter(fromDate) match {
      case true => Some(ActivityReportFormData(fromDate, toDate))
      case _ => None
    }
  }

}