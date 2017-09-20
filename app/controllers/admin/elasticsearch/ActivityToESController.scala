package controllers.admin.elasticsearch

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import org.joda.time.DateTime
import org.quartz.{JobBuilder, JobKey}
import play.mvc.Http.Flash
import services.elasticsearch.ActivityESService
import services.job.ReindexActivityJob
import services.{ActivityService, SchedulerService, SecurityService}
import system.Roles

@Singleton
class ActivityToESController @Inject()(
  security: SecurityService,
  activityService: ActivityService,
  activityESService: ActivityESService,
  scheduler: SchedulerService
) extends BaseController {

  import Roles._
  import security._
  import play.api.data._
  import play.api.data.Forms._

  val formData = Form(
    mapping(
      "fromDate" -> default(jodaDate("yyyy-MM-dd'T'HH:mm:ss"), defaultFormData.fromDate),
      "toDate" -> default(jodaDate("yyyy-MM-dd'T'HH:mm:ss"), defaultFormData.toDate)
    )(ActivityToESControllerFormData.apply)(ActivityToESControllerFormData.unapply) verifying(
      """"From" date must be before "To" date""",
      fields => fields match {
        case data => ActivityToESControllerFormData.validate(data.fromDate, data.toDate).isDefined
      })
  )

  def defaultFormData = {
    val defaultTo = DateTime.now()
    val defaultFrom = defaultTo.minusDays(7)
    ActivityToESControllerFormData(defaultFrom, defaultTo)
  }

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.elasticsearch.index(formData, defaultFormData))
  }

  def reindexActivitiesInDateTimeRange = RequiredActualUserRoleAction(Sysadmin) { implicit request =>

    formData.bindFromRequest.fold(
      formError => {
        Redirect(controllers.admin.elasticsearch.routes.ActivityToESController.index()).flashing("error" -> formError.errors.map(_.message).mkString(", "))
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