package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.news.{Audience, NotificationData}
import models.{ActivitySave, ActivityRecipients}
import warwick.sso.Usercode

import scala.util.Try

object NotificationPublishingService {
  val PROVIDER_ID = "news"
  val ACTIVITY_TYPE = "news"
}

@ImplementedBy(classOf[NotificationPublishingServiceImpl])
trait NotificationPublishingService {

  def publish(item: NotificationData, audience: Audience): Try[String]

}

@Singleton
class NotificationPublishingServiceImpl @Inject()(
  activityService: ActivityService,
  audienceService: AudienceService
) extends NotificationPublishingService {

  import NotificationPublishingService._

  def publish(item: NotificationData, audience: Audience): Try[String] =
    audienceService.resolve(audience)
      .flatMap(recipients => activityService.save(makeActivitySave(item), recipients))

  private def makeActivitySave(item: NotificationData) =
    ActivitySave(
      providerId = PROVIDER_ID,
      `type` = ACTIVITY_TYPE,
      title = item.text,
      url = item.linkHref,
      shouldNotify = true
    )

}
