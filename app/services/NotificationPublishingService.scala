package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.news.{Audience, NotificationSave}
import models.{ActivitySave, PublishedNotificationSave}
import org.joda.time.DateTime
import org.quartz.JobBuilder.newJob
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityDao, AudienceDao, PublishedNotificationsDao}
import services.job.PublishActivityJob

object NotificationPublishingService {
  val ACTIVITY_TYPE = "news"
}

@ImplementedBy(classOf[NotificationPublishingServiceImpl])
trait NotificationPublishingService {

  def publish(item: NotificationSave, audience: Audience): String

}

@Singleton
class NotificationPublishingServiceImpl @Inject()(
  activityDao: ActivityDao,
  audienceDao: AudienceDao,
  scheduler: SchedulerService,
  publishedNotificationsDao: PublishedNotificationsDao,
  @NamedDatabase("default") db: Database
) extends NotificationPublishingService {

  import NotificationPublishingService._

  def publish(item: NotificationSave, audience: Audience): String = {
    db.withTransaction { implicit c =>
      val audienceId = audienceDao.saveAudience(audience)
      val activityId = activityDao.save(makeActivitySave(item, audienceId), Seq.empty)

      publishedNotificationsDao.save(PublishedNotificationSave(
        activityId = activityId,
        publisherId = item.publisherId,
        createdBy = item.usercode
      ))

      schedulePublishJob(audienceId, activityId, item.publishDate)

      activityId
    }
  }

  private def makeActivitySave(item: NotificationSave, audienceId: String) =
    ActivitySave(
      providerId = item.providerId,
      `type` = ACTIVITY_TYPE,
      title = item.text,
      url = item.linkHref,
      shouldNotify = true,
      audienceId = Some(audienceId),
      generatedAt = Some(item.publishDate)
    )

  private def schedulePublishJob(audienceId: String, activityId: String, publishDate: DateTime): Unit = {
    val job = newJob(classOf[PublishActivityJob])
      .withIdentity(activityId, "PublishActivity")
      .usingJobData("activityId", activityId)
      .usingJobData("audienceId", audienceId)
      .build()

    if (publishDate.isAfterNow) {
      val trigger = newTrigger()
        .startAt(publishDate.toDate)
        .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
        .build()

      scheduler.scheduleJob(job, trigger)
    } else {
      scheduler.triggerJobNow(job)
    }
  }

}
