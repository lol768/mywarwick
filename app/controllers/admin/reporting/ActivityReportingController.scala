package controllers.admin.reporting

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import org.joda.time.{DateTime, Interval}
import play.api.data.Form
import play.api.data.Forms.{jodaDate, mapping}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, Result}
import services.SecurityService
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

  def renderResultWithInterval(
    interval: Interval,
    formError: Option[Form[ActivityReportFormData]] = None
  )(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] = {
    import system.ThreadPools.web
    if (formError.isDefined) {
      return Future.successful(Ok(views.html.admin.reporting.activity.index(
        null,
        formError.orNull
      )))
    }

    for {
      allAlertsByProviders <- activityReportingService.allAlertsByProviders(interval)
    } yield {
      val sortedResult = ListMap(allAlertsByProviders.toSeq.sortBy {
        case (provider, _) => provider.displayName.getOrElse(provider.id)
      }: _*)
      Ok(views.html.admin.reporting.activity.index(
        sortedResult,
        formData.fill(ActivityReportFormData(interval.getStart, interval.getEnd))
      ))
    }
  }


  def index = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    val defaultFormData = ActivityReportFormData.withDefaultTimeDate()
    renderResultWithInterval(new Interval(defaultFormData.fromDate, defaultFormData.toDate))
  }

  def formSubmit = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>

    formData.bindFromRequest.fold(
      formError => {
        renderResultWithInterval(ActivityReportFormData.makeDefaultTimeDate(), Some(formError))
      },
      data => {
        renderResultWithInterval(new Interval(data.fromDate, data.toDate))
      }
    )
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

  def withDefaultTimeDate() = {
    val default = makeDefaultTimeDate()
    ActivityReportFormData(default.getStart, default.getEnd)
  }

  def makeDefaultTimeDate() = {
    val defaultTo = DateTime.now()
    val defaultFrom = defaultTo.minusDays(14)
    new Interval(defaultFrom, defaultTo)
  }
}