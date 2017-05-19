package system

import java.util.Date

import com.google.inject.{Inject, Singleton}
import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz._
import services.job.{APNSInactiveDeviceCleanupJob, ActivityMuteCleanupJob, MessageSendCleanupJob}

@Singleton
class SchedulerConfiguration @Inject()(
  implicit scheduler: Scheduler
) extends Logging {

  configureScheduledJob(
    "MessageSendCleanupJob",
    newJob(classOf[MessageSendCleanupJob]),
    dailyAtHourAndMinute(1, 0)
  )

  configureScheduledJob(
    "APNSInactiveDeviceCleanupJob",
    newJob(classOf[APNSInactiveDeviceCleanupJob]),
    dailyAtHourAndMinute(1, 30)
  )

  configureScheduledJob(
    "ActivityMuteCleanupJob",
    newJob(classOf[ActivityMuteCleanupJob]),
    dailyAtHourAndMinute(3, 0)
  )

  scheduler.start()

  def configureScheduledJob[SBT <: Trigger](name: String, jobBuilder: JobBuilder, schedule: ScheduleBuilder[SBT])(implicit scheduler: Scheduler): Option[Date] = {
    val jobKey = new JobKey(name)

    if (scheduler.getJobDetail(jobKey) == null) {
      val job = jobBuilder.withIdentity(jobKey).build()
      val trigger = newTrigger().withSchedule[SBT](schedule).build().asInstanceOf[Trigger]

      logger.info(s"Scheduling job: $name")
      Some(scheduler.scheduleJob(job, trigger))
    } else {
      logger.info(s"Job already scheduled: $name")
      None
    }
  }

}
