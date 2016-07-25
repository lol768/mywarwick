package helpers

import com.google.inject.Singleton
import org.quartz.JobDetail
import services.ScheduleJobService

@Singleton
class MockScheduleJobService extends ScheduleJobService {
  override def triggerJobNow(job: JobDetail): Unit = Nil
}
