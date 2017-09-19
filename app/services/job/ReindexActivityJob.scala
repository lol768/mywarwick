package services.job

import java.util.UUID
import javax.inject.Inject

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.quartz._
import services.ActivityService
import services.elasticsearch.{ActivityESService, IndexActivityRequest}

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
class ReindexActivityJob @Inject()(
  activityESService: ActivityESService,
  activityService: ActivityService
) extends Job with ReindexActivityJobHelper {

  override def execute(context: JobExecutionContext) = {
    val dateTimeRange = getDateTimeRangeFromContext(context)
    val activities = activityService.getActivitiesForDateTimeRange(
      dateTimeRange.get(jobDateKeyForFromDate).orNull,
      dateTimeRange.get(jobDateKeyForToDate).orNull
    )
    activities.grouped(1000).foreach(group => activityESService.index(group.map(IndexActivityRequest(_))))
  }
}

trait ReindexActivityJobHelper {

  val jobType: Class[ReindexActivityJob] = classOf[ReindexActivityJob]
  val jobId = new JobKey(UUID.randomUUID().toString, "ReindexActivityJob")
  val jobDateKeyForFromDate = "fromDate"
  val jobDateKeyForToDate = "toDate"
  val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")


  def getDateTimeRangeFromContext(context: JobExecutionContext): Map[String, DateTime] = {
    val data: JobDataMap = context.getJobDetail.getJobDataMap
    val fromDate: DateTime = DateTime.parse(data.getString(jobDateKeyForFromDate), dateTimeFormat)
    val toDate: DateTime = DateTime.parse(data.getString(jobDateKeyForToDate), dateTimeFormat)
    Map(
      jobDateKeyForFromDate -> fromDate,
      jobDateKeyForToDate -> toDate
    )
  }
}

object ReindexActivityJobHelper extends ReindexActivityJobHelper