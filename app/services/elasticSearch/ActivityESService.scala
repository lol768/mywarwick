package services.elasticSearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Activity
import models.publishing.Publisher
import org.joda.time.DateTime
import services.{AudienceService, PublisherService}
import warwick.sso.Usercode

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(activity: Activity)
}

@Singleton
class ActivityESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  audienceService: AudienceService,
  publisherService: PublisherService
) extends ActivityESService {

  private val client = eSClientConfig.newClient

  def indexNameToday(): String = {
    s"""activity_${DateTime.now().toString("yyyy_mm_dd")}"""
  }

  override def index(activity: Activity): Unit = {
    val id = activity.id
    val providerId = activity.providerId
    val activityType = activity.`type`
    val title = activity.title
    val text = activity.text.getOrElse("-")
    val url = activity.url.getOrElse("-")
    val replacedBy = activity.replacedBy.getOrElse("-")
    val publishedAt = activity.publishedAt.toDate
    val shouldNotify = activity.shouldNotify
    val audiences: Seq[String] = activity.audienceId match {
      case Some(id: String) => audienceService.resolve(audienceService.getAudience(id)).getOrElse(Seq(Usercode("-"))).map(_.toString())
      case _ => Seq("-")
    }
    val publisher: String = activity.publisherId match {
      case Some(id: String) => publisherService.find(id) match {
        case Some(e: Publisher) => e.id
        case _ => "-"
      }
      case _ => "-"
    }

    import org.elasticsearch.common.xcontent.XContentFactory._
    val builder = jsonBuilder().startObject()
    builder
      .field("id", "kimchy")
      .field("provide_id", providerId)
      .field("type", activityType)
      .field("title", title)
      .field("url", url)
      .field("replaced_by", replacedBy)
      .field("published_at", publishedAt)
      .field("should_notify", shouldNotify)
      .field("publisher", publisher)

    builder.startArray("audiences")
    audiences.foreach(audience => {
      builder.startObject()
      builder.field("id", audience)
      builder.endObject()
    })
    builder.endArray()

  }

}