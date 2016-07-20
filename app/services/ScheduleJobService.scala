package services

import com.google.inject.{ImplementedBy, Inject}
import org.quartz.TriggerBuilder._
import org.quartz.{JobDetail, Scheduler}

@ImplementedBy(classOf[ScheduleJobServiceImpl])
trait ScheduleJobService {
  def triggerJobNow(job: JobDetail): Unit
}

class ScheduleJobServiceImpl @Inject()(
  scheduler: Scheduler
) extends ScheduleJobService {

  private val immediateJobTrigger = newTrigger().startNow().build()

  def triggerJobNow(job: JobDetail) =
    scheduler.scheduleJob(job, immediateJobTrigger)
}
