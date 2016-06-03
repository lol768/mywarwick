package services

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import models.news.{Audience, NotificationData}
import models.{ActivityPrototype, ActivityRecipients}
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
      .map(makeActivityPrototype(item))
      .flatMap(activityService.save)

  private def makeActivityPrototype(item: NotificationData)(usercodes: Seq[Usercode]) =
    ActivityPrototype(
      providerId = PROVIDER_ID,
      `type` = ACTIVITY_TYPE,
      title = item.text,
      text = None,
      url = item.linkHref,
      tags = Seq.empty,
      replace = Map.empty,
      generatedAt = None,
      shouldNotify = true,
      recipients = ActivityRecipients(
        users = Some(usercodes.map(_.string)),
        groups = None
      )
    )

}
