package services.elasticSearch

import java.util
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Audience.{DepartmentAudience, ModuleAudience, UsercodeAudience, WebGroupAudience}
import models.Activity
import models.publishing.Publisher
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.{GetRequest, GetResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.search.{SearchHit, SearchHits}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import services.{AudienceService, PublisherService}
import warwick.core.Logging
import warwick.sso.Usercode

import scala.concurrent.{Future, Promise}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(activity: Activity)

  // get as in elasticsearch get api
  def getDocumentByActivityId(activityId: String, isNotification: Boolean = true): Future[ActivityDocument]

  def deleteDocumentByActivityId(activityId: String, isNotification: Boolean = true)

  // match all
  def search(activityESSearchQuery: ActivityESSearchQuery): Seq[ActivityDocument]

}

@Singleton
class ActivityESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  audienceService: AudienceService,
  publisherService: PublisherService
) extends ActivityESService with Logging {

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
      activityId
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
        getResponsePromise.failure(e)
      }
    })
    futureResponse
      .map(ActivityDocument.fromESGetResponse)
      .recover {
        case exception =>
          logger.error("Exceptions thrown after sending a elasticsearch GetRequest", exception)
          ActivityDocument()
      }
  }

  override def deleteDocumentByActivityId(activityId: String, isNotification: Boolean): Unit = ???

  override def search(input: ActivityESSearchQuery): Seq[ActivityDocument] = {
    val helper = ActivityESServiceSearchHelper
    val searchSourceBuilder = new SearchSourceBuilder()
    val searchRequest = new SearchRequest(ActivityESServiceSearchHelper.indexNameForAllTime())
    val boolQueryBuilder = new BoolQueryBuilder()

    input.provider_id match {
      case Some(provider_id) => boolQueryBuilder.should(QueryBuilders.termQuery(helper.ESFieldName.provider_id, provider_id))
      case _ =>
    }

    input.activity_type match {
      case Some(activity_type) => boolQueryBuilder.should(QueryBuilders.termQuery(helper.ESFieldName.activity_type, activity_type))
      case _ =>
    }


    input.publish_at match {
      case Some(dateRange) =>
      case _ =>
    }

    input.publisher match {
      case Some(publisher) => boolQueryBuilder.should(QueryBuilders.termQuery(helper.ESFieldName.publisher, publisher))
      case _ =>
    }

    input.text match {
      case Some(text) =>
      case _ =>
    }

    input.title match {
      case Some(title) => boolQueryBuilder.should(QueryBuilders.termQuery(helper.ESFieldName.title, title))
      case _ =>
    }

    input.url match {
      case Some(url) => boolQueryBuilder.should(QueryBuilders.termQuery(helper.ESFieldName.url, url))
      case _ =>
    }

    searchSourceBuilder.query(boolQueryBuilder)
    searchRequest.types(ActivityESServiceSearchHelper.documentType)
    searchRequest.source(searchSourceBuilder)

    client.searchAsync(searchRequest, new ActionListener[SearchResponse] {
      override def onResponse(response: SearchResponse) = {
        val hits = response.getHits.asScala.toList

      }

      override def onFailure(e: Exception) = {

      }
    })

  }
}

trait ActivityESServiceHelper {

  val documentType = "activity" // we use the same type for both alert and activity. they are the same structure but in different indexes
  val nameForAlert = "alert"
  val nameForActivity = "activity"
  val separator = "_"

  object ESFieldName {
    val provider_id = "provider_id"
    val activity_type = "activity_type"
    val title = "title"
    val url = "url"
    val text = "text"
    val replaced_by = "replaced_by"
    val published_at = "published_at"
    val publisher = "publisher"
    val resolved_users = "resolved_users"
    val audience_components = "audience_components"
  }

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

object ActivityESServiceGetHelper extends ActivityESServiceHelper

object ActivityESServiceDeleteHelper extends ActivityESServiceHelper

object ActivityESServiceSearchHelper extends ActivityESServiceHelper

object ActivityESServiceIndexHelper extends ActivityESServiceHelper {

  def makeIndexRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): IndexRequest = {
    new IndexRequest(indexName, docType, docId).source(docSource)
  }

  def makeIndexDocBuilder(activityDocument: ActivityDocument): XContentBuilder = {
    import org.elasticsearch.common.xcontent.XContentFactory._
    val builder: XContentBuilder = jsonBuilder().startObject()

    builder
      .field(ESFieldName.provider_id, activityDocument.provider_id)
      .field(ESFieldName.activity_type, activityDocument.activity_type)
      .field(ESFieldName.title, activityDocument.title)
      .field(ESFieldName.url, activityDocument.url)
      .field(ESFieldName.text, activityDocument.text)
      .field(ESFieldName.replaced_by, activityDocument.replaced_by)
      .field(ESFieldName.published_at, activityDocument.published_at)
      .field(ESFieldName.publisher, activityDocument.publisher)

    builder.startArray(ESFieldName.resolved_users)
    activityDocument.resolvedUsers.foreach(builder.value)
    builder.endArray()

    builder.startArray(ESFieldName.audience_components)
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

case class ActivityESSearchQuery(
  provider_id: Option[String] = Option.empty,
  activity_type: Option[String] = Option.empty,
  title: Option[String] = Option.empty,
  url: Option[String] = Option.empty,
  text: Option[String] = Option.empty,
  replaced_by: Option[String] = Option.empty,
  publish_at: Option[ActivityESSearchQuery.DateRange] = Option.empty,
  publisher: Option[String] = Option.empty,
  audienceComponents: Option[Seq[String]] = Option.empty,
  resolvedUsers: Option[Seq[String]] = Option.empty,
)

object ActivityESSearchQuery {

  case class DateRange(from: Date, to: Date)

}

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

  def fromESGetResponse(res: GetResponse): ActivityDocument = {
    val helper = ActivityESServiceGetHelper
    val audience_components = res
      .getField(helper.ESFieldName.audience_components)
      .getValues
      .asScala
      .toList
      .map(_.toString)

    val resolved_users = res
      .getField(helper.ESFieldName.resolved_users)
      .getValues
      .asScala
      .map(_.toString)

    ActivityDocument(
      res.getField(helper.ESFieldName.provider_id).getValue.toString,
      res.getField(helper.ESFieldName.activity_type).getValue.toString,
      res.getField(helper.ESFieldName.title).getValue.toString,
      res.getField(helper.ESFieldName.url).getValue.toString,
      res.getField(helper.ESFieldName.text).getValue.toString,
      res.getField(helper.ESFieldName.replaced_by).getValue.toString,
      res.getField(helper.ESFieldName.published_at).getValue,
      res.getField(helper.ESFieldName.publisher).getValue.toString,
      audience_components,
      resolved_users,
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