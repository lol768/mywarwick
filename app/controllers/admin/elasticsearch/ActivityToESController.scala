package controllers.admin.elasticsearch

import javax.inject.Inject

import controllers.BaseController
import models.Activity
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.quartz.{JobBuilder, JobKey}
import services.elasticsearch.{ActivityESService, IndexActivityRequest}
import services.job.ReindexActivityJob
import services.{ActivityService, SchedulerService, SecurityService}
import system.Roles

import scala.collection.immutable.Queue

@Inject
class ActivityToESController @Inject()(
  security: SecurityService,
  activityService: ActivityService,
  activityESService: ActivityESService,
  scheduler: SchedulerService
) extends BaseController {

  import Roles._
  import security._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.elasticsearch.index())
  }

  def reindexAllActivities = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    //TODO bind start and end dates
    import services.job.ReindexActivityJobHelper._

    val fromDate: DateTime = DateTime.parse("2010-11-01T01:00+02:00")
    val toDate: DateTime = DateTime.parse("2017-12-01T01:00+02:00")

    val jobDetail = JobBuilder
      .newJob()
      .ofType(jobType)
      .withIdentity(jobId)
      .usingJobData(jobDateKeyForFromDate, fromDate.toString(dateTimeFormat))
      .usingJobData(jobDateKeyForToDate, toDate.toString(dateTimeFormat))
      .build()
    scheduler.triggerJobNow(jobDetail)

    Ok(views.html.admin.elasticsearch.index())
  }
}

case class formData(fromDate: DateTime, toDate: DateTime)
