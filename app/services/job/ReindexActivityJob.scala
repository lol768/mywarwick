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

    val dateTimeRange = getDateTimeRangeFromContext(context)

    val bigInterval: Interval = new Interval(
      dateTimeRange.get(jobDateKeyForFromDate).orNull,
      dateTimeRange.get(jobDateKeyForToDate).orNull
    )

    val period = Period.days(1)

    subRange(bigInterval, period)
      .foreach(shorterInterval => activityService
        .getActivitiesForDateTimeRange(shorterInterval)
        .grouped(1000)
        .foreach(group => activityESService.index(group.map(IndexActivityRequest(_))))
      )
  }
}

object ReindexActivityJobHelper {

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

  def subRange(bigInterval: Interval, period: Period): Seq[Interval] = {
    val size: Int = BigDecimal(bigInterval.toDuration.getStandardSeconds.toDouble / period.toStandardDuration.getStandardSeconds.toDouble).setScale(0, BigDecimal.RoundingMode.UP).toInt
    Range(0, size).map(i => {
      val start = bigInterval.getStartMillis + i * period.toStandardDuration.getMillis
      val end = (i + 1) == size match {
        case true => bigInterval.getEndMillis
        case false => start + period.toStandardDuration.getMillis
      }
      new Interval(start, end)
    })
  }

}