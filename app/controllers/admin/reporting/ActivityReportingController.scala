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
    formError: Form[ActivityReportFormData] = null
  )(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] = {
    import system.ThreadPools.elastic

    if (formError != null) {
      return Future(Ok(views.html.admin.reporting.activity.index(
        null,
        formError
      )))
    }

    for {
      allAlertsByProviders <- Future.sequence(activityReportingService.allAlertsByProviders(interval).map {
        case (provider, futureDocs) =>
          for {
            docs <- futureDocs
          } yield (provider, docs)
      }).map(_.toSeq.sortBy {
        case (provider, _) => provider.displayName.getOrElse(provider.id)
      })
    } yield {
      Ok(views.html.admin.reporting.activity.index(
        ListMap(allAlertsByProviders: _*),
        formData.fill(ActivityReportFormData(interval.getStart, interval.getEnd))
      ))
    }
  }


  def index = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    val defaultFormData = ActivityReportFormData.withDefaultTimeDate()
    //    formData.fill(defaultFormData)
    renderResultWithInterval(new Interval(defaultFormData.fromDate, defaultFormData.toDate))
  }

  def formSubmit = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>

    formData.bindFromRequest.fold(
      formError => {
        renderResultWithInterval(ActivityReportFormData.makeDefaultTimeDate(), formError)
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