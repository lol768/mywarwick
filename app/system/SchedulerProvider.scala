package system

import java.util.Date

import com.google.inject.{Inject, Provider, Singleton}
import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import services.job.{APNSInactiveDeviceCleanupJob, MessageSendCleanupJob}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchedulerProvider @Inject()(jobFactory: GuiceJobFactory, lifecycle: ApplicationLifecycle) extends Provider[Scheduler] {

  val logger = Logger(getClass)

  def get(): Scheduler = {
    import ExecutionContext.Implicits.global

    implicit val scheduler = new StdSchedulerFactory().getScheduler
    scheduler.setJobFactory(jobFactory)

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

    scheduler.start()

    def shutdown: Future[Unit] = Future {
      // Waits for running jobs to finish.
      scheduler.shutdown(true)
    }

    lifecycle.addStopHook(shutdown _)

    scheduler
  }

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
