package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Activity
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.bulk.{BulkRequest, BulkRequestBuilder, BulkResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{Response, RestClient, RestHighLevelClient}
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue, Json}
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

  def count(activityESSearchQuery: ActivityESSearchQuery): Future[Int]

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
  private val lowLevelClient: RestClient = eSClientConfig.lowLevelClient

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

  // TODO we are not using the search function at all at the moent, but if one day we do, this should be reimplemented with search_after to get over the 10k limit
  // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-search-after.html
  override def search(input: ActivityESSearchQuery): Future[Seq[ActivityDocument]] = {
    val helper = ActivityESServiceSearchHelper

    val searchRequest = new SearchRequest(ActivityESServiceSearchHelper.indexNameForActivitySearchQuery(input))
    val boolQueryBuilder: BoolQueryBuilder = helper.makeBoolQueryBuilder(input)
    val searchSourceBuilder: SearchSourceBuilder = helper.makeSearchSourceBuilder(boolQueryBuilder)

    searchRequest.types(ActivityESServiceSearchHelper.documentType)
    searchRequest.source(searchSourceBuilder)

    val listener = new FutureActionListener[SearchResponse]
    client.searchAsync(searchRequest, listener)

    listener.future
      .map(ActivityDocument.fromESSearchResponse)
      .recover {
        case exception =>
          logger.error("Exceptions thrown after sending a elasticsearch SearchRequest", exception)
          Seq()
      }
  }

  override def count(input: ActivityESSearchQuery): Future[Int] = {
    val lowHelper = LowLevelClientHelper
    lowHelper.performRequestAsync(
      method = lowHelper.Method.get,
      path = lowHelper.makePathForCountApiFromActivityEsSearchQuery(input),
      entity = Some(lowHelper.httpEntityFromJsValue(lowHelper.makeQueryForCountApiFromActivityESSearchQuery(input))),
      lowLevelClient = lowLevelClient
    ).map {
      lowHelper.getCountFromCountApiRes
    }
  }
}