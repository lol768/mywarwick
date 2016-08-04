package services.job

import actors.WebsocketActor.Notification
import com.google.inject.Inject
import models.{Activity, ActivityResponse}
import org.quartz._
import services._
import services.messaging.MessagingService
import system.Logging
import warwick.sso.Usercode

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
trait PublishingJob extends Job with Logging {

  val scheduler: ScheduleJobService

  def executeJob(context: JobExecutionContext): Unit

  override def execute(c: JobExecutionContext): Unit =
    try {
      executeJob(c)
    }
    catch {
      case e: Exception => scheduler.maybeRetryJob(c, e)
    }
}

class PublishNewsItemJob @Inject()(
  audienceService: AudienceService,
  newsService: NewsService,
  override val scheduler: ScheduleJobService
) extends PublishingJob {

  override def executeJob(context: JobExecutionContext): Unit = {

    val dataMap = context.getJobDetail.getJobDataMap
    val newsItemId = dataMap.getString("newsItemId")
    val audienceId = dataMap.getString("audienceId")

    val recipients = audienceService.resolve(
      audienceService.getAudience(audienceId)
    ).get // FIXME Try.get throws
    newsService.setRecipients(newsItemId, recipients)
  }
}

class PublishActivityJob @Inject()(
  audienceService: AudienceService,
  activityService: ActivityService,
  messaging: MessagingService,
  pubSub: PubSub,
  override val scheduler: ScheduleJobService
) extends PublishingJob {

  override def executeJob(context: JobExecutionContext): Unit = {

    val dataMap = context.getJobDetail.getJobDataMap
    val activityId = dataMap.getString("activityId")
    val audienceId = dataMap.getString("audienceId")

    val audience = audienceService.getAudience(audienceId)

    audienceService.resolve(audience).foreach { recipients =>
      activityService.getActivityById(activityId).foreach { activity =>
        saveRecipients(activityId, activity, recipients)
      }
    }
  }

  private def saveRecipients(id: String, activity: Activity, recipients: Seq[Usercode]) = {
    messaging.send(recipients.toSet, activity)
    val activityResponse = ActivityResponse(
      activity,
      activityService.getActivityIcon(id),
      Seq.empty
    )
    recipients.foreach(usercode => pubSub.publish(usercode.string, Notification(activityResponse)))
  }
}
