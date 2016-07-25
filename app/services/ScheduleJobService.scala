package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import org.joda.time.DateTime
import org.quartz.TriggerBuilder._
import org.quartz.{JobDetail, JobExecutionContext, Scheduler}
import play.api.Configuration
import system.Logging

@ImplementedBy(classOf[ScheduleJobServiceImpl])
trait ScheduleJobService extends Logging {
  val FAILED_ATTEMPTS = "FAILED_ATTEMPTS"

  def triggerJobNow(job: JobDetail): Unit

  def maybeRetryJob(context: JobExecutionContext, e: Exception): Unit
}

@Singleton
class ScheduleJobServiceImpl @Inject()(
  scheduler: Scheduler,
  config: Configuration
) extends ScheduleJobService {

  private val RETRY_TIMES = config.getInt("quartz.default.retryAttempts")
    .getOrElse(throw new IllegalStateException("Missing Quartz job retry attempt times - set quartz.retryAttempts"))

  private val retryWaitSecs = config.getInt("quartz.default.retryWaitSecs")
    .getOrElse(throw new IllegalStateException("Missing Quartz job retry wait time - set quartz.retryWaitSecs"))

  private def immediateJobTrigger = newTrigger.startNow.build

  override def triggerJobNow(job: JobDetail) =
    scheduler.scheduleJob(job, immediateJobTrigger)

  override def maybeRetryJob(c: JobExecutionContext, e: Exception) = {
    if (incrementJobAttempts(c) < RETRY_TIMES) {
      logger.warn(s"Job ${c.getJobDetail.getJobClass} failed, retrying in $retryWaitSecs seconds", e)
      val startTime = DateTime.now.plusSeconds(retryWaitSecs)
      val trigger = newTrigger.startAt(startTime.toDate).build
      scheduler.rescheduleJob(c.getTrigger.getKey, trigger)
    } else {
      logger.error(s"Job ${c.getJobDetail.getKey.getName} failed $RETRY_TIMES times", e)
    }
  }

  private def incrementJobAttempts(context: JobExecutionContext) = {
    val map = context.getJobDetail.getJobDataMap
    val updatedAttempts = try { map.getInt(FAILED_ATTEMPTS) + 1 } catch { case e: ClassCastException => 0 }
    map.put(FAILED_ATTEMPTS, updatedAttempts)
    updatedAttempts
  }
}
