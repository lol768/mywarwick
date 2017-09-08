package services.elasticSearch

import java.util.Date
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Audience.{DepartmentAudience, ModuleAudience, UsercodeAudience, WebGroupAudience}
import models.{Activity}
import models.publishing.Publisher
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.client.RestHighLevelClient
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

  private val client: RestHighLevelClient = eSClientConfig.newClient

  override def index(activity: Activity): Unit = {
    val activityDocument = ActivityDocument.fromActivityModel(
      activity,
      audienceService,
      publisherService
    )
    val docBuilder = ActivityESService.makeIndexDocBuilder(activityDocument)
    val indexName = ActivityESService.indexNameToday(activity.shouldNotify)
    val request = ActivityESService.makeIndexRequest(indexName, "activity", activity.id, docBuilder)

    client.indexAsync(request, new ActionListener[IndexResponse] {
      override def onFailure(e: Exception) = {
        println("error sending activity to es")
        println(e)
      }

      override def onResponse(response: IndexResponse) = {
        println("response from es")
        println(response.toString)
      }
    })
  }
}

object ActivityESService {
  def indexNameToday(isNotification: Boolean): String = {
    val today = DateTime.now().toString("yyyy_MM")
    isNotification match {
      case true => s"""alert_$today"""
      case false => s"""activity_$today"""
    }
  }

  def makeIndexRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): IndexRequest = {
    new IndexRequest(indexName, docType, docId).source(docSource)
  }

  def makeIndexDocBuilder(activityDocument: ActivityDocument): XContentBuilder = {
    import org.elasticsearch.common.xcontent.XContentFactory._
    val builder: XContentBuilder = jsonBuilder().startObject()

    builder
      .field("provider_id", activityDocument.provider_id)
      .field("activity_type", activityDocument.activity_type)
      .field("title", activityDocument.title)
      .field("url", activityDocument.url)
      .field("text", activityDocument.text)
      .field("replaced_by", activityDocument.replaced_by)
      .field("published_at", activityDocument.published_at)
      .field("publisher", activityDocument.publisher)

    builder.startArray("resolved_users")
    activityDocument.resolvedUsers.foreach(builder.value)
    builder.endArray()

    builder.startArray("audience_components")
    activityDocument.audienceComponents.foreach(builder.value)
    builder.endArray()

    builder.endObject()
    builder
  }

}

case class ActivityDocument(
  provider_id: String,
  activity_type: String,
  title: String,
  url: String,
  text: String,
  replaced_by: String,
  published_at: Date,
  publisher: String,
  audienceComponents: Seq[String],
  resolvedUsers: Seq[String]
)

object ActivityDocument {
  def fromActivityModel(
    activity: Activity,
    audienceService: AudienceService,
    publisherService: PublisherService
  ): ActivityDocument = {
    ActivityDocument(
      activity.providerId,
      activity.`type`,
      activity.title,
      activity.url.getOrElse("-"),
      activity.text.getOrElse("-"),
      activity.replacedBy.getOrElse("-"),
      activity.publishedAt.toDate,
      serialisePublisher(activity.publisherId, publisherService),
      serialiseAudienceComponents(activity.audienceId, audienceService),
      serialiseResolvedUsers(activity.audienceId, audienceService)
    )
  }

  def serialiseAudienceComponents(audienceId: Option[String], audienceService: AudienceService): Seq[String] = {
    audienceId match {
      case Some(id: String) => audienceService.getAudience(id).components.flatMap {
        case e: UsercodeAudience => Seq("usercode")
        case e: WebGroupAudience => Seq(s"""WebGroupAudience:${e.groupName.string}""")
        case e: ModuleAudience => Seq(s"""ModuleAudience:${e.moduleCode}""")
        case e: DepartmentAudience => e.subset.map(subset => {
          s"""DepartmentAudience:${e.deptCode}:${subset.entryName}"""
        })
        case _ => Nil
      }
      case _ => Nil
    }
  }

  def serialiseResolvedUsers(audienceId: Option[String], audienceService: AudienceService): Seq[String] = {
    audienceId match {
      case Some(id: String) => audienceService.resolve(audienceService.getAudience(id))
        .getOrElse(Seq(Usercode("-")))
        .map(_.string)
      case _ => Seq("-")
    }
  }

  def serialisePublisher(publisherId: Option[String], publisherService: PublisherService): String = {
    publisherId match {
      case Some(id: String) => publisherService.find(id) match {
        case Some(e: Publisher) => e.id
        case _ => "-"
      }
      case _ => "-"
    }
  }
}