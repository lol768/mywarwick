package helpers

import org.quartz.{JobDetail, JobExecutionContext, JobKey, Trigger}
import services.ScheduleJobService

class MockScheduleJobService extends ScheduleJobService {

  var triggeredJobs: Seq[JobDetail] = Seq.empty
  var scheduledJobs: Seq[JobDetailAndTrigger] = Seq.empty
  var deletedJobs: Seq[JobKey] = Seq.empty

  override def triggerJobNow(job: JobDetail) = {
    triggeredJobs = triggeredJobs :+ job
  }

  override def maybeRetryJob(context: JobExecutionContext, e: Exception) = ()

  override def scheduleJob(job: JobDetail, trigger: Trigger) = {
    scheduledJobs = scheduledJobs :+ JobDetailAndTrigger(job, trigger)
  }

  override def deleteJob(key: JobKey) = {
    deletedJobs = deletedJobs :+ key
  }

  def reset() = {
    triggeredJobs = Seq.empty
    scheduledJobs = Seq.empty
    deletedJobs = Seq.empty
  }

}

case class JobDetailAndTrigger(job: JobDetail, trigger: Trigger)