package services.elasticSearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Activity
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.{GetRequest, GetResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.search.builder.SearchSourceBuilder
import services.{AudienceService, PublisherService}
import warwick.core.Logging

import scala.concurrent.{ExecutionContext, Future, Promise}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(activity: Activity)

  def update(activity: Activity)


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

  //TODO implement me https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-document-update.html
  override def update(activity: Activity): Unit = ???

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
        logger.error("Exception raised when sending indexRequest to ES", e)
      }

      override def onResponse(response: IndexResponse) = {
        logger.debug("IndexRequest sent to ES with response: " + response.toString)
      }
    })
  }

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
  //TODO move some logics to helper object
  //rough start of the search function, could be entirely incorrect
  override def search(input: ActivityESSearchQuery): Future[Seq[ActivityDocument]] = {
    val helper = ActivityESServiceSearchHelper
    val searchSourceBuilder = new SearchSourceBuilder()
    val searchRequest = new SearchRequest(ActivityESServiceSearchHelper.indexNameForAllTime())
    val boolQueryBuilder = new BoolQueryBuilder()

    input.provider_id match {
      case Some(provider_id) => boolQueryBuilder.must(QueryBuilders.termQuery(helper.ESFieldName.provider_id, provider_id))
      case _ =>
    }

    input.activity_type match {
      case Some(activity_type) => boolQueryBuilder.must(QueryBuilders.termQuery(helper.ESFieldName.activity_type, activity_type))
      case _ =>
    }


    //TODO figure out the range query
    input.publish_at match {
      case Some(dateRange) =>
      case _ =>
    }

    input.publisher match {
      case Some(publisher) => boolQueryBuilder.must(QueryBuilders.termQuery(helper.ESFieldName.publisher, publisher))
      case _ =>
    }

    //TODO figure out fuzzy text match
    //TODO check if text is analysed or not
    input.text match {
      case Some(text) =>
      case _ =>
    }

    input.title match {
      case Some(title) => boolQueryBuilder.must(QueryBuilders.termQuery(helper.ESFieldName.title, title))
      case _ =>
    }

    input.url match {
      case Some(url) => boolQueryBuilder.must(QueryBuilders.termQuery(helper.ESFieldName.url, url))
      case _ =>
    }

    //TODO figure out query for array
    input.audienceComponents match {
      case Some(components) =>
      case _ =>
    }

    //TODO figure out query for array
    input.resolvedUsers match {
      case Some(resolvedUsers) =>
      case _ =>
    }

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