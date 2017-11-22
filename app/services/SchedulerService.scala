package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import org.joda.time.DateTime
import org.quartz.TriggerBuilder._
import org.quartz._
import play.api.Configuration
import system.Logging

@ImplementedBy(classOf[SchedulerServiceImpl])
trait SchedulerService extends Logging {
  val FAILED_ATTEMPTS = "FAILED_ATTEMPTS"

  def triggerJobNow(job: JobDetail): Unit

  def maybeRetryJob(context: JobExecutionContext, e: Exception): Unit

  def scheduleJob(job: JobDetail, trigger: Trigger): Unit

  def deleteJob(key: JobKey): Unit
}

@Singleton
class SchedulerServiceImpl @Inject()(
  scheduler: Scheduler,
  config: Configuration
) extends SchedulerService {

  private val RETRY_TIMES = config.get[Int]("quartz.job.retryAttempts")

  private val retryWaitSecs = config.get[Int]("quartz.job.retryWaitSecs")

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
      logger.error(s"Job ${c.getJobDetail.getJobClass} failed $RETRY_TIMES times", e)
    }
  }

  override def scheduleJob(job: JobDetail, trigger: Trigger) = {
    scheduler.scheduleJob(job, trigger)
  }

  override def deleteJob(key: JobKey) = {
    scheduler.deleteJob(key)
  }

  private def incrementJobAttempts(context: JobExecutionContext) = {
    val map = context.getJobDetail.getJobDataMap
    val updatedAttempts = try { map.getInt(FAILED_ATTEMPTS) + 1 } catch { case e: ClassCastException => 0 }
    map.put(FAILED_ATTEMPTS, updatedAttempts)
    updatedAttempts
  }
}
