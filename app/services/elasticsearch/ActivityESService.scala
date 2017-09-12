package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Activity
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.{GetRequest, GetResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.search.builder.SearchSourceBuilder
import services.{AudienceService, PublisherService}
import warwick.core.Logging
import warwick.sso.Usercode
import collection.JavaConverters._

import scala.concurrent.{ExecutionContext, Future, Promise}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None)

  def update(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None)

  // get as in elasticsearch get api
  def getDocumentByActivityId(activityId: String, isNotification: Boolean = true): Future[ActivityDocument]

  def deleteDocumentByActivityId(activityId: String, isNotification: Boolean = true)

  // match all
  def search(activityESSearchQuery: ActivityESSearchQuery): Future[Seq[ActivityDocument]]

}

@Singleton
class ActivityESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  audienceService: AudienceService,
  publisherService: PublisherService
) extends ActivityESService with Logging {

  private val client: RestHighLevelClient = eSClientConfig.newClient

  override def index(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None): Unit = {
    val activityDocument = ActivityDocument.fromActivityModel(
      activity,
      audienceService,
      publisherService,
      resolvedUsers
    )
    val helper = ActivityESServiceIndexHelper

    val docBuilder = helper.elasticSearchContentBuilderFromActivityDocument(activityDocument)
    val indexName = helper.indexNameToday(activity.shouldNotify)
    val request = helper.makeIndexRequest(indexName, helper.documentType, activity.id, docBuilder)

    client.indexAsync(request, new ActionListener[IndexResponse] {
      override def onFailure(e: Exception) = {
        logger.error("Exception thrown when sending indexRequest to ES", e)
      }

      override def onResponse(response: IndexResponse) = {
        logger.debug("IndexRequest sent to ES with response: " + response.toString)
      }
    })
  }

  override def update(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None) = index(activity, resolvedUsers)

  // rough start of the implementation, might be entirely incorrect
  override def getDocumentByActivityId(activityId: String, isNotification: Boolean = true): Future[ActivityDocument] = {
    val helper = ActivityESServiceGetHelper
    val request = new GetRequest(
      helper.indexNameForAllTime(isNotification),
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
    import ExecutionContext.Implicits.global
    futureResponse
      .map(ActivityDocument.fromESGetResponse)
      .recover {
        case exception =>
          logger.error("Exceptions thrown after sending a elasticsearch GetRequest", exception)
          ActivityDocument()
      }
  }

  //TODO implement me https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-document-delete.html
  override def deleteDocumentByActivityId(activityId: String, isNotification: Boolean): Unit = ???

  //TODO extract into small functions so that we can have unit tests
  override def search(input: ActivityESSearchQuery): Future[Seq[ActivityDocument]] = {
    val helper = ActivityESServiceSearchHelper
    val searchSourceBuilder = new SearchSourceBuilder()
    val searchRequest = new SearchRequest(ActivityESServiceSearchHelper.indexNameForAllTime())
    val boolQueryBuilder: BoolQueryBuilder = helper.makeBoolQueryBuilder(input)

    searchSourceBuilder.query(boolQueryBuilder)
    searchRequest.types(ActivityESServiceSearchHelper.documentType)
    searchRequest.source(searchSourceBuilder)

    val searchResponsePromise: Promise[SearchResponse] = Promise[SearchResponse]
    val futureResponse: Future[SearchResponse] = searchResponsePromise.future
    client.searchAsync(searchRequest, new ActionListener[SearchResponse] {
      override def onFailure(e: Exception) = {
        searchResponsePromise.failure(e)
      }

      override def onResponse(response: SearchResponse) = {
        searchResponsePromise.success(response)
      }
    })
    import ExecutionContext.Implicits.global
    futureResponse
      .map(ActivityDocument.fromESSearchResponse)
      .recover {
        case exception =>
          logger.error("Exceptions thrown after sending a elasticsearch SearchRequest", exception)
          Seq()
      }
  }
}