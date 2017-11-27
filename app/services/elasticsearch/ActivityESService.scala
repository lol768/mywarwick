package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Activity
import org.elasticsearch.action.{ActionListener, DocWriteRequest}
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

  override def index(req: IndexActivityRequest): Future[Unit] = index(Seq(req))

  def index(reqs: Seq[IndexActivityRequest]): Future[Unit] = {
    val writeReqs = reqs.map { req =>
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

      helper.makeIndexRequest(indexName, helper.documentType, activity.id, docBuilder)
    }
    val bulkRequest = new BulkRequest().add(writeReqs : _*)
    val listener = new FutureActionListener[BulkResponse]
    client.bulkAsync(bulkRequest, listener)
    listener.future.map { response =>
      if (response.hasFailures) {
        logger.error(response.buildFailureMessage)
      }
      () // Unit
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