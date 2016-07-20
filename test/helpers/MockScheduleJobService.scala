package helpers

import org.quartz.JobDetail
import services.ScheduleJobService

class MockScheduleJobService extends ScheduleJobService {
  override def triggerJobNow(job: JobDetail): Unit = Nil
}
