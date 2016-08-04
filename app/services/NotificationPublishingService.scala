package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.news.{Audience, NotificationSave}
import models.{ActivitySave, PublishedNotificationSave}
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityDao, AudienceDao, PublishedNotificationsDao}

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
  publishedNotificationsDao: PublishedNotificationsDao,
  @NamedDatabase("default") db: Database
) extends NotificationPublishingService {

  import NotificationPublishingService._

  def publish(item: NotificationSave, audience: Audience): String = {
    def makeActivitySave(item: NotificationSave, audienceId: String) =
      ActivitySave(
        providerId = item.providerId,
        `type` = ACTIVITY_TYPE,
        title = item.text,
        url = item.linkHref,
        shouldNotify = true,
        audienceId = Some(audienceId),
        generatedAt = Some(item.publishDate)
      )

    db.withTransaction { implicit c =>
      val audienceId = audienceDao.saveAudience(audience)
      val activityId = activityDao.save(makeActivitySave(item, audienceId), Seq.empty)

      publishedNotificationsDao.save(PublishedNotificationSave(
        activityId = activityId,
        publisherId = item.publisherId,
        createdBy = item.usercode
      ))

      activityId
    }
  }
}
