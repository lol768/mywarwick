package controllers.admin.elasticsearch

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import org.joda.time.DateTime
import org.quartz.{JobBuilder, JobKey}
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
      "fromDate" -> text,
      "toDate" -> text
    )(ActivityToESControllerFormData.apply)(ActivityToESControllerFormData.unapply))

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.elasticsearch.index(formData))
  }

  def reindexActivitiesInDateTimeRange = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val data = formData.bindFromRequest.get

    val fromDate: DateTime = new DateTime(data.fromDate)
    val toDate: DateTime = new DateTime(data.toDate)

    import services.job.ReindexActivityJobHelper._
    val jobDetail = JobBuilder
      .newJob()
      .ofType(jobType)
      .withIdentity(jobId)
      .usingJobData(jobDateKeyForFromDate, fromDate.toString(dateTimeFormat))
      .usingJobData(jobDateKeyForToDate, toDate.toString(dateTimeFormat))
      .build()
    scheduler.triggerJobNow(jobDetail)

    Ok(views.html.admin.elasticsearch.index(formData))
  }
}

case class ActivityToESControllerFormData(fromDate: String, toDate: String)
