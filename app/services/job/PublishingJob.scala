package services.job

import actors.WebSocketActor.Notification
import com.google.inject.Inject
import models.ActivityRender
import org.quartz._
import services._
import services.elasticsearch.ActivityESService
import services.messaging.MessagingService
import system.Logging
import warwick.sso.Usercode

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
trait PublishingJob extends Job with Logging {

  val scheduler: SchedulerService

  def executeJob(context: JobExecutionContext): Unit

  override def execute(c: JobExecutionContext): Unit =
    try {
      executeJob(c)
    }
    catch {
      case e: Exception => scheduler.maybeRetryJob(c, e)
    }
}

object PublishNewsItemJob {
  val name = "PublishNewsItem"
}

class PublishNewsItemJob @Inject()(
  audienceService: AudienceService,
  newsService: NewsService,
  override val scheduler: SchedulerService
) extends PublishingJob {

  override def executeJob(context: JobExecutionContext): Unit = {
    val dataMap = context.getJobDetail.getJobDataMap
    val newsItemId = dataMap.getString("newsItemId")
    val audienceId = dataMap.getString("audienceId")

    val recipients = audienceService.resolve(
      audienceService.getAudience(audienceId)
    ).get // FIXME Try.get throws
    newsService.setRecipients(newsItemId, recipients)
    newsService.updateAudienceCount(newsItemId)
  }
}

object PublishActivityJob {
  val name = "PublishActivity"
}

class PublishActivityJob @Inject()(
  audienceService: AudienceService,
  activityService: ActivityService,
  messaging: MessagingService,
  pubSub: PubSub,
  activityESService: ActivityESService,
  override val scheduler: SchedulerService
) extends PublishingJob {

  override def executeJob(context: JobExecutionContext): Unit = {
    val dataMap = context.getJobDetail.getJobDataMap
    val activityId = dataMap.getString("activityId")
    val audienceId = dataMap.getString("audienceId")

    val audience = audienceService.getAudience(audienceId)

    audienceService.resolve(audience).foreach { recipients =>
      activityService.getActivityRenderById(activityId).foreach { activityRender =>
        saveRecipients(activityRender, recipients.toSet)
        activityService.updateAudienceCount(activityId, audienceId, recipients)
      }
    }
  }

  private def saveRecipients(activityRender: ActivityRender, recipients: Set[Usercode]) = {
    val activity = activityRender.activity
    activityService.setRecipients(activity, recipients)

    val notification = Notification(activityRender)
    recipients.foreach(usercode => pubSub.publish(usercode.string, notification))

    if (activity.shouldNotify) {
      messaging.send(recipients, activity)
    }
    activityESService.index(activity)
  }
}
