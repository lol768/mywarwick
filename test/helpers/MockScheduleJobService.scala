package helpers

import com.google.inject.Singleton
import org.quartz.{JobDetail, JobExecutionContext}
import services.ScheduleJobService

@Singleton
class MockScheduleJobService extends ScheduleJobService {
  override def triggerJobNow(job: JobDetail): Unit = Nil
  override def maybeRetryJob(context: JobExecutionContext): Unit = Nil
}
