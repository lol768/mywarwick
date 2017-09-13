package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Activity
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.index.{IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.{BoolQueryBuilder}
import org.elasticsearch.search.builder.SearchSourceBuilder
import services.{AudienceService, PublisherService}
import warwick.core.Logging
import warwick.sso.Usercode

import scala.concurrent.{ExecutionContext, Future, Promise}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None)

  def index(activities: Seq[Activity])

  def update(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None)

  def deleteDocumentByActivityId(activityId: String, isNotification: Boolean = true)

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


  override def index(activities: Seq[Activity]): Unit = {
    activities.foreach(activity => {
      this.index(activity)
    })
  }

  override def update(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None) = index(activity, resolvedUsers)

  //TODO implement me https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-document-delete.html
  override def deleteDocumentByActivityId(activityId: String, isNotification: Boolean): Unit = ???

  override def search(input: ActivityESSearchQuery): Future[Seq[ActivityDocument]] = {
    val helper = ActivityESServiceSearchHelper

    val searchRequest = new SearchRequest(ActivityESServiceSearchHelper.indexNameForAllTime())
    val boolQueryBuilder: BoolQueryBuilder = helper.makeBoolQueryBuilder(input)
    val searchSourceBuilder: SearchSourceBuilder = helper.makeSearchSourceBuilder(boolQueryBuilder)

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