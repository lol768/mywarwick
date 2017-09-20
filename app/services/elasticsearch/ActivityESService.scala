package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Activity
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.bulk.{BulkRequest, BulkRequestBuilder, BulkResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import services.{AudienceService, PublisherService}
import warwick.core.Logging
import warwick.sso.Usercode
import system.ThreadPools.elastic
import scala.concurrent.{ExecutionContext, Future, Promise}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(req: IndexActivityRequest): Future[Unit]

  def index(requests: Seq[IndexActivityRequest]): Future[Unit]

  def deleteDocumentByActivityId(activityId: String, isNotification: Boolean = true)

  def search(activityESSearchQuery: ActivityESSearchQuery): Future[Seq[ActivityDocument]]

}

case class IndexActivityRequest(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None)

@Singleton
class ActivityESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  audienceService: AudienceService,
  publisherService: PublisherService,
  elasticSearchAdminService: ElasticSearchAdminService
) extends ActivityESService with Logging {

  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.activityEsTemplates, "activity_template_default")
  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.alertEsTemplates, "alert_template_default")

  private val client: RestHighLevelClient = eSClientConfig.highLevelClient

  override def index(req: IndexActivityRequest) = index(Seq(req))

  def index(reqs: Seq[IndexActivityRequest]): Future[Unit] = {
    val bulkRequest = new BulkRequest()
    reqs.foreach { req =>
      val activity = req.activity
      val resolvedUsers = req.resolvedUsers
      val activityDocument = ActivityDocument.fromActivityModel(
        activity,
        audienceService,
        publisherService,
        resolvedUsers
      )
      val helper = ActivityESServiceIndexHelper

      val docBuilder = helper.elasticSearchContentBuilderFromActivityDocument(activityDocument)

      val indexName = helper.indexNameForActivity(activity)

      val indexRequest = helper.makeIndexRequest(indexName, helper.documentType, activity.id, docBuilder)

      bulkRequest.add(indexRequest)
    }

    val listener = new FutureActionListener[BulkResponse]
    client.bulkAsync(bulkRequest, listener)
    listener.future.map(_ => {})
  }

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
    futureResponse
      .map(ActivityDocument.fromESSearchResponse)
      .recover {
        case exception =>
          logger.error("Exceptions thrown after sending a elasticsearch SearchRequest", exception)
          Seq()
      }
  }
}