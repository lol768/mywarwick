package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.news.{Audience, NotificationSave}
import models.{ActivitySave, PublishedNotificationSave}
import play.api.db.{Database, NamedDatabase}
import services.dao.{AudienceDao, PublishedNotificationsDao}

import scala.util.Try

object NotificationPublishingService {
  val PROVIDER_ID = "news"
  val ACTIVITY_TYPE = "news"
}

@ImplementedBy(classOf[NotificationPublishingServiceImpl])
trait NotificationPublishingService {

  def publish(item: NotificationSave, audience: Audience): Try[Either[Seq[ActivityError], String]]

}

@Singleton
class NotificationPublishingServiceImpl @Inject()(
  activityService: ActivityService,
  audienceService: AudienceService,
  audienceDao: AudienceDao,
  publishedNotificationsDao: PublishedNotificationsDao,
  @NamedDatabase("default") db: Database
) extends NotificationPublishingService {

  import NotificationPublishingService._

  def publish(item: NotificationSave, audience: Audience): Try[Either[Seq[ActivityError], String]] = {
    val audienceId = db.withTransaction(implicit c => audienceDao.saveAudience(audience))

    // FIXME: move audience resolution and subsequent notification publishing to scheduler
    audienceService.resolve(audience)
      .map { recipients =>
        activityService.save(makeActivitySave(item, audienceId), recipients)
          .fold(
            errors => Left(errors),
            activityId =>
              db.withTransaction { implicit c =>
                publishedNotificationsDao.save(PublishedNotificationSave(
                  activityId = activityId,
                  publisherId = item.publisherId,
                  createdBy = item.usercode
                ))

                Right(activityId)
              }
          )
      }
  }

  private def makeActivitySave(item: NotificationSave, audienceId: String) =
    ActivitySave(
      providerId = item.providerId,
      `type` = ACTIVITY_TYPE,
      title = item.text,
      url = item.linkHref,
      shouldNotify = true,
      audienceId = Some(audienceId)
    )

}
