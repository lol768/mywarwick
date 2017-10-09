package controllers.admin.elasticsearch

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import org.joda.time.DateTime
import org.quartz.JobBuilder
import play.api.i18n.{I18nSupport, MessagesApi}
import services.elasticsearch.ActivityESService
import services.{ActivityService, SchedulerService, SecurityService}
import system.Roles
import play.api.data._
import play.api.data.Forms._

@Singleton
class ActivityToESController @Inject()(
  security: SecurityService,
  activityService: ActivityService,
  activityESService: ActivityESService,
  scheduler: SchedulerService,
  val messagesApi: MessagesApi
) extends BaseController with I18nSupport {

  import Roles._
  import security._

  object FormField {
    val from = "fromDate"
    val to = "toDate"
  }

  val formData = Form(
    mapping(
      "fromDate" -> jodaDate("yyyy-MM-dd'T'HH:mm:ss"),
      "toDate" -> jodaDate("yyyy-MM-dd'T'HH:mm:ss")
    )(ActivityToESControllerFormData.apply)(ActivityToESControllerFormData.unapply) verifying(
      """"From" date must be before "To" date""",
      fields => fields match {
        case data => ActivityToESControllerFormData.validate(data.fromDate, data.toDate).isDefined
      })
  )

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val defaultTo = DateTime.now()
    val defaultFrom = defaultTo.minusDays(7)
    ActivityToESControllerFormData(defaultFrom, defaultTo)
    Ok(views.html.admin.elasticsearch.index(formData.fill(ActivityToESControllerFormData(defaultFrom, defaultTo))))
  }

  def reindexActivitiesInDateTimeRange = RequiredActualUserRoleAction(Sysadmin) { implicit request =>

    formData.bindFromRequest.fold(
      formError => {
        Ok(views.html.admin.elasticsearch.index(formError))
      },
      data => {
        // do something with the data
        val fromDate: DateTime = data.fromDate
        val toDate: DateTime = data.toDate

        import services.job.ReindexActivityJobHelper._
        val jobDetail = JobBuilder
          .newJob()
          .ofType(jobType)
          .withIdentity(jobId)
          .usingJobData(jobDateKeyForFromDate, fromDate.toString(dateTimeFormat))
          .usingJobData(jobDateKeyForToDate, toDate.toString(dateTimeFormat))
          .build()
        scheduler.triggerJobNow(jobDetail)
        Redirect(controllers.admin.elasticsearch.routes.ActivityToESController.index()).flashing("success" -> "The task is now added to scheduler queue.")
      }
    )
  }
}

case class ActivityToESControllerFormData(fromDate: DateTime, toDate: DateTime)

object ActivityToESControllerFormData {
  def validate(fromDate: DateTime, toDate: DateTime) = {
    toDate.isAfter(fromDate) match {
      case true => Some(ActivityToESControllerFormData(fromDate, toDate))
      case _ => None
    }
  }
}