package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.ActivitySave
import models.news.{Audience, NotificationData}
import play.api.db.{Database, NamedDatabase}
import services.dao.PublishCategoryDao

import scala.util.Try

object NotificationPublishingService {
  val PROVIDER_ID = "news"
  val ACTIVITY_TYPE = "news"
}

@ImplementedBy(classOf[NotificationPublishingServiceImpl])
trait NotificationPublishingService {

  def publish(item: NotificationData, audience: Audience, categoryIds: Seq[String]): Try[String]

}

@Singleton
class NotificationPublishingServiceImpl @Inject()(
  activityService: ActivityService,
  audienceService: AudienceService,
  publishCategoryDao: PublishCategoryDao,
  @NamedDatabase("default") db: Database
) extends NotificationPublishingService {

  import NotificationPublishingService._

  def publish(item: NotificationData, audience: Audience, categoryIds: Seq[String]): Try[String] =
    audienceService.resolve(audience)
      .flatMap(recipients => activityService.save(makeActivitySave(item), recipients))
      .map { id =>
        db.withConnection(implicit c => publishCategoryDao.saveNotificationCategories(id, categoryIds))
        id
      }

  private def makeActivitySave(item: NotificationData) =
    ActivitySave(
      providerId = PROVIDER_ID,
      `type` = ACTIVITY_TYPE,
      title = item.text,
      url = item.linkHref,
      shouldNotify = true
    )

}
