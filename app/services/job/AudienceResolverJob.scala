package services.job

import actors.WebsocketActor.Notification
import com.google.inject.Inject
import models.{Activity, ActivityResponse}
import org.quartz.{Job, JobExecutionContext}
import services.messaging.MessagingService
import services.{ActivityService, AudienceService, NewsService, PubSub}
import system.Logging
import warwick.sso.Usercode

class NewsAudienceResolverJob @Inject()(
  audienceService: AudienceService,
  newsService: NewsService
) extends Job with Logging {

  override def execute(context: JobExecutionContext): Unit = {

    val dataMap = context.getJobDetail.getJobDataMap
    val newsItemId = dataMap.getString("newsItemId")
    val audienceId = dataMap.getString("audienceId")
    val isUpdate = dataMap.getBoolean("isUpdate")

    if (isUpdate) newsService.deleteRecipients(newsItemId)

    val recipients = audienceService.resolve(
      audienceService.getAudience(audienceId)
    ).get // FIXME Try.get throws
    newsService.saveRecipients(newsItemId, recipients)
  }
}

class NotificationsAudienceResolverJob @Inject()(
  audienceService: AudienceService,
  activityService: ActivityService,
  messaging: MessagingService,
  pubSub: PubSub
) extends Job with Logging {

  override def execute(context: JobExecutionContext): Unit = {

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
