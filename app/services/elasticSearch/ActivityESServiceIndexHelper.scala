package services.elasticSearch

import java.util.Date
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Audience.{DepartmentAudience, ModuleAudience, UsercodeAudience, WebGroupAudience}
import models.Activity
import models.publishing.Publisher
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.{GetRequest, GetResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentBuilder
import org.joda.time.DateTime
import services.{AudienceService, PublisherService}
import warwick.sso.Usercode

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(activity: Activity)

  // get as in elasticsearch get api
  def getDocumentByActivityId(activityId: String, isNotification: Boolean = true): Future[ActivityDocument]

  def deleteDocumentByActivityId(activityId: String, isNotification: Boolean = true)

  def search(): Seq[ActivityDocument]

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
    val helper = ActivityESServiceIndexHelper

    val docBuilder = helper.makeIndexDocBuilder(activityDocument)
    val indexName = helper.indexNameToday(activity.shouldNotify)
    val request = helper.makeIndexRequest(indexName, helper.documentType, activity.id, docBuilder)

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

  override def getDocumentByActivityId(activityId: String, isNotification: Boolean): Future[ActivityDocument] = {
    val helper = ActivityESServiceGetHelper
    val request = new GetRequest(
      helper.indexNameForAllTime(),
      helper.documentType,
      "1" // we only have 1 version per document
    )
    val getResponsePromise: Promise[GetResponse] = Promise[GetResponse]
    val futureResponse: Future[GetResponse] = getResponsePromise.future
    client.getAsync(request, new ActionListener[GetResponse] {
      @Override
      override def onResponse(getResponse: GetResponse): Unit = {
        getResponsePromise.success(getResponse)
      }

      @Override
      override def onFailure(e: Exception): Unit = {
        // TODO log the exception
        getResponsePromise.failure(e)
      }
    })

    futureResponse.map(res => {
      ActivityDocument() // and add some data
    }).recover(error => {
      ActivityDocument() // empty document
    })
    // but if it's a failed future, how do i return a Future[empty document]?
  }

  override def deleteDocumentByActivityId(activityId: String, isNotification: Boolean): Unit = ???

  override def search(): Seq[ActivityDocument] = ???
}

trait ActivityESServiceHelper {

  val documentType = "activity" // we use the same type for both alert and activity. they are the same structure but in different indexes
  val nameForAlert = "alert"
  val nameForActivity = "activity"
  val separator = "_"

  def indexNameToday(isNotification: Boolean = true): String = {
    val today = DateTime.now().toString("yyyy_MM")
    isNotification match {
      case true => s"""$nameForAlert$separator$today"""
      case false => s"""$nameForActivity$separator$today"""
    }
  }

  def indexNameForAllTime(isNotification: Boolean = true): String = {
    isNotification match {
      case true => s"""$nameForAlert*"""
      case false => s"""$nameForActivity*"""
    }
  }
}

object ActivityESServiceGetHelper extends ActivityESServiceHelper {

}

object ActivityESServiceDeleteHelper extends ActivityESServiceHelper {

}


object ActivityESServiceIndexHelper extends ActivityESServiceHelper {

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
  provider_id: String = "-",
  activity_type: String = "-",
  title: String = "-",
  url: String = "-",
  text: String = "-",
  replaced_by: String = "-",
  published_at: Date = new Date(0),
  publisher: String = "-",
  audienceComponents: Seq[String] = Seq("-"),
  resolvedUsers: Seq[String] = Seq("-")
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