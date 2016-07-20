package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.{ActivitySave, PublishedNotificationSave}
import models.news.{Audience, NotificationSave}
import org.quartz.TriggerBuilder._
import org.quartz.{JobBuilder, Scheduler}
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityDao, AudienceDao, PublishedNotificationsDao}
import services.job.{NewsAudienceResolverJob, NotificationsAudienceResolverJob}
import system.SchedulerProvider
import warwick.sso.Usercode

import scala.util.Try

object NotificationPublishingService {
  val PROVIDER_ID = "news"
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
  scheduler: SchedulerProvider,
  publishedNotificationsDao: PublishedNotificationsDao,
  @NamedDatabase("default") db: Database
) extends NotificationPublishingService {

  import NotificationPublishingService._

  private val immediateJobTrigger = newTrigger().startNow().build()

  def publish(item: NotificationSave, audience: Audience): String = {
    def makeActivitySave(item: NotificationSave, audienceId: String) =
      ActivitySave(
        providerId = PROVIDER_ID,
        `type` = ACTIVITY_TYPE,
        title = item.text,
        url = item.linkHref,
        shouldNotify = true,
        audienceId = Some(audienceId)
      )

    db.withTransaction { implicit c =>
      val audienceId = audienceDao.saveAudience(audience)
      val activityId = activityDao.save(makeActivitySave(item, audienceId), Seq.empty)

      publishedNotificationsDao.save(PublishedNotificationSave(
        activityId = activityId,
        publisherId = item.publisherId,
        createdBy = item.usercode
      ))

      val job = JobBuilder.newJob(classOf[NotificationsAudienceResolverJob])
        .usingJobData("activityId", activityId)
        .usingJobData("audienceId", audienceId)
        .build()

      scheduler.triggerJobNow(job)

      activityId
    }
  }
}
