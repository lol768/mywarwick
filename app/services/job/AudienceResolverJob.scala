package services.job

import actors.WebsocketActor.Notification
import com.google.inject.Inject
import models.{Activity, ActivityIcon, ActivityResponse}
import org.quartz.{Job, JobExecutionContext}
import play.api.db.Database
import services.dao.{ActivityTagDao, NewsDao, PublishedNotificationsDao}
import services.messaging.MessagingService
import services.{ActivityService, AudienceService, PubSub}
import system.Logging
import warwick.sso.Usercode

class NewsAudienceResolverJob @Inject()(
  audienceService: AudienceService,
  dao: NewsDao,
  db: Database
) extends Job with Logging {

  override def execute(context: JobExecutionContext): Unit = db.withTransaction { implicit c =>

    val dataMap = context.getJobDetail.getJobDataMap
    val newsItemId = dataMap.getString("newsItemId")
    val audienceId = dataMap.getString("audienceId")
    val isUpdate = dataMap.getBoolean("isUpdate")

    if (isUpdate) dao.deleteRecipients(newsItemId)

    val recipients = audienceService.resolve(
      audienceService.getAudience(audienceId)
    ).get // FIXME Try.get throws
    dao.saveRecipients(newsItemId, recipients)
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
//      activityService.getActivityIcon(id),
      None,
      Seq.empty
    )
    recipients.foreach(usercode => pubSub.publish(usercode.string, Notification(activityResponse)))
  }
}
