package services.elasticSearch

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s
import com.sksamuel.elastic4s.{ArrayFieldValue, FieldValue, Indexable, SimpleFieldValue}
import models.Activity
import models.publishing.Publisher
import org.elasticsearch.common.xcontent.XContentBuilder
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

  private val client = eSClientConfig.getElastic4sTcpClient()

  def indexNameToday(): String = {
    s"""activity_${DateTime.now().toString("yyyy_mm_dd")}"""
  }

  override def index(activity: Activity): Unit = {

    //    val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
    //    val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")


    val id = activity.id
    val providerId = activity.providerId
    val activityType = activity.`type`
    val title = activity.title
    val text = activity.text.getOrElse("-")
    val url = activity.url.getOrElse("-")
    val replacedBy = activity.replacedBy.getOrElse("-")
    val publishedAt = activity.publishedAt
    val shouldNotify = activity.shouldNotify
    val audiences: Seq[String] = activity.audienceId match {
      case Some(id: String) => audienceService.resolve(audienceService.getAudience(id)).getOrElse(Seq(Usercode("-"))).map(_.toString())
    }
    val publisher: String = activity.publisherId match {
      case Some(id: String) => publisherService.find(id) match {
        case Some(e: Publisher) => e.id
        case _ => "-"
      }
      case _ => "-"
    }

    import com.sksamuel.elastic4s.ElasticDsl._
    indexInto(indexNameToday() / activityType) fieldValues(
      SimpleFieldValue("id", id),
      SimpleFieldValue("ptovider_id", providerId),
      SimpleFieldValue("type", activityType),
      SimpleFieldValue("title", title),
      SimpleFieldValue("text", text),
      SimpleFieldValue("url", url),
      SimpleFieldValue("replaced_by", replacedBy),
      DateTimeField("published_at", publishedAt),
      elastic4s.SimpleFieldValue("should_notify", shouldNotify),
      ArrayFieldValue("audiences", audiences.map(SimpleFieldValue(_))),
      SimpleFieldValue("publisher", publisher)
    )

  }

}

case class DateTimeField(name: String, date: org.joda.time.DateTime) extends FieldValue {

  private val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"

  def output(source: XContentBuilder): Unit = {
    source.field(name, new SimpleDateFormat(dateFormat).format(date.toString(dateFormat)))
  }

}