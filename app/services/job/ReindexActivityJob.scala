package services.job

import java.util.UUID
import javax.inject.Inject

import org.joda.time.{DateTime, Interval, Period}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.quartz._
import services.ActivityService
import services.elasticsearch.{ActivityESService, IndexActivityRequest}

import scala.math.BigDecimal

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
class ReindexActivityJob @Inject()(
  activityESService: ActivityESService,
  activityService: ActivityService
) extends Job {

  def execute(context: JobExecutionContext) = {
    import ReindexActivityJobHelper._
    toSmallerIntervals(getDateTimeRangeFromContext(context), Period.hours(6))
      .foreach(activityService.getActivitiesForDateTimeRange(_)
        .grouped(1000).map(_.map(IndexActivityRequest(_, None)))
        .foreach(activityESService.indexActivityReqs)
      )
  }
}

object ReindexActivityJobHelper {

  val jobType: Class[ReindexActivityJob] = classOf[ReindexActivityJob]
  val jobId = new JobKey(UUID.randomUUID().toString, "ReindexActivityJob")
  val jobDateKeyForFromDate = "fromDate"
  val jobDateKeyForToDate = "toDate"
  val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")

  def getDateTimeRangeFromContext(context: JobExecutionContext): Interval = {
    val data: JobDataMap = context.getJobDetail.getJobDataMap
    val fromDate: DateTime = DateTime.parse(data.getString(jobDateKeyForFromDate), dateTimeFormat)
    val toDate: DateTime = DateTime.parse(data.getString(jobDateKeyForToDate), dateTimeFormat)
    new Interval(fromDate, toDate)
  }

  def toSmallerIntervals(bigInterval: Interval, smallIntervalSize: Period): Seq[Interval] = {
    val size: Int = BigDecimal(bigInterval.toDuration.getStandardSeconds.toDouble / smallIntervalSize.toStandardDuration.getStandardSeconds.toDouble).setScale(0, BigDecimal.RoundingMode.UP).toInt
    Range(0, size).map(i => {
      val start = bigInterval.getStartMillis + i * smallIntervalSize.toStandardDuration.getMillis
      val end = if ((i + 1) == size) {
        bigInterval.getEndMillis
      } else {
        start + smallIntervalSize.toStandardDuration.getMillis
      }
      new Interval(start, end)
    })
  }

}