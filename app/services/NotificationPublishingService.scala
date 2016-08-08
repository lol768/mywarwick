package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.news.{Audience, NotificationSave}
import models.{ActivitySave, PublishedNotificationSave}
import org.joda.time.DateTime
import org.quartz.JobBuilder.newJob
import org.quartz.JobKey
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import play.api.db.{Database, NamedDatabase}
import services.ActivityError.{AlreadyPublished, DoesNotExist}
import services.dao.{ActivityDao, AudienceDao, PublishedNotificationsDao}
import services.job.PublishActivityJob

object NotificationPublishingService {
  val ACTIVITY_TYPE = "news"
}

@ImplementedBy(classOf[NotificationPublishingServiceImpl])
trait NotificationPublishingService {
  def update(id: String, item: NotificationSave, audience: Audience): Either[Seq[ActivityError], Unit]

  def publish(item: NotificationSave, audience: Audience): Either[Seq[ActivityError], String]

  def delete(id: String): Either[Seq[ActivityError], Unit]
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

  override def publish(item: NotificationSave, audience: Audience) = db.withTransaction { implicit c =>
    val audienceId = audienceDao.saveAudience(audience)
    val activityId = activityDao.save(makeActivitySave(item, audienceId), Seq.empty)

    publishedNotificationsDao.save(PublishedNotificationSave(
      activityId = activityId,
      publisherId = item.publisherId,
      changedBy = item.usercode
    ))

    schedulePublishJob(activityId, audienceId, item.publishDate)

    Right(activityId)
  }

  override def update(activityId: String, item: NotificationSave, audience: Audience) = db.withTransaction { implicit c =>
    activityDao.getActivityById(activityId) match {
      case Some(activity) if activity.generatedAt.isAfterNow =>
        val audienceId = audienceDao.saveAudience(audience)
        activityDao.update(activityId, makeActivitySave(item, audienceId))

        publishedNotificationsDao.update(PublishedNotificationSave(
          activityId = activityId,
          publisherId = item.publisherId,
          changedBy = item.usercode
        ))

        schedulePublishJob(activityId, audienceId, item.publishDate)

        Right(())
      case Some(_) =>
        Left(Seq(AlreadyPublished))
      case None =>
        Left(Seq(DoesNotExist))
    }
  }

  override def delete(id: String) = db.withTransaction { implicit c =>
    activityDao.getActivityById(id) match {
      case Some(activity) if activity.generatedAt.isAfterNow =>
        publishedNotificationsDao.delete(id)

        activity.audienceId.foreach(audienceDao.deleteAudience)
        activityDao.delete(id)

        unschedulePublishJob(id)

        Right(())
      case Some(_) =>
        Left(Seq(AlreadyPublished))
      case None =>
        Left(Seq(DoesNotExist))
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

  private def schedulePublishJob(activityId: String, audienceId: String, publishDate: DateTime): Unit = {
    val key = new JobKey(activityId, "PublishActivity")

    scheduler.deleteJob(key)

    val job = newJob(classOf[PublishActivityJob])
      .withIdentity(key)
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

  private def unschedulePublishJob(activityId: String): Unit = {
    scheduler.deleteJob(new JobKey(activityId, "PublishActivity"))
  }

}
