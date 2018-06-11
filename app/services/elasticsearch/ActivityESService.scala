package services.elasticsearch

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named, Singleton}
import models.{Activity, MessageState, Output}
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.elasticsearch.common.xcontent.{XContentBuilder, XContentFactory}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.aggregations.{AggregationBuilders, Aggregations}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import services.elasticsearch.ActivityESSearch.CountQueryResponse
import services.elasticsearch.ActivityESServiceHelper.ESFieldName
import services.{AudienceService, PublisherService}
import warwick.core.Logging
import warwick.sso.Usercode

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class IndexActivityRequest(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None)

case class MessageSent(activityId: String, usercode: Usercode, state: MessageState, output: Output)

object MessageSent {
  implicit val reads: Reads[MessageSent] = (
    (JsPath \ "_source" \ "activity_id").read[String] ~
      (JsPath \ "_source" \ "usercode").read[String].map(Usercode) ~
      (JsPath \ "_source" \ "state").read[String].map(s => MessageState.parse(s)) ~
      (JsPath \ "_source" \ "output").read[String].map(s => Output.parse(s))
    ) (MessageSent.apply _)
}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  val helper = ActivityESServiceIndexHelper

  def indexActivityReq(req: IndexActivityRequest): Future[Unit]

  def indexActivityReqs(requests: Seq[IndexActivityRequest]): Future[Unit]

  def indexMessageSentReq(req: MessageSent): Future[Unit]

  def indexMessageSentReqs(reqs: Seq[MessageSent]): Future[Unit]

  def count(activityESSearchQuery: ActivityESSearch.SearchQuery): Future[ActivityESSearch.CountQueryResponse]

  def deliveryReportForActivity(activityId: String, publishedAt: Option[DateTime]): Future[AlertDeliveryReport]
}

@Singleton
class ActivityESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  audienceService: AudienceService,
  publisherService: PublisherService,
  elasticSearchAdminService: ElasticSearchAdminService
)(implicit @Named("elastic") ec: ExecutionContext) extends ActivityESService with Logging {

  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.activityEsTemplates, "activity_template_default")
  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.alertEsTemplates, "alert_template_default")
  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.deliveryReportEsTemplates, "delivery_report_template_default")

  private val client: RestHighLevelClient = eSClientConfig.highLevelClient
  private val lowLevelClient: RestClient = eSClientConfig.lowLevelClient

  override def indexActivityReq(req: IndexActivityRequest): Future[Unit] = indexActivityReqs(Seq(req))

  private def makeBulkRequest(writeReqs: Seq[IndexRequest]): Future[Unit] = {
    val bulkRequest = new BulkRequest().add(writeReqs: _*)
    val listener = new FutureActionListener[BulkResponse]
    client.bulkAsync(bulkRequest, listener)
    listener.future.map { response =>
      if (response.hasFailures) {
        logger.error(response.buildFailureMessage)
      }
      () // Unit
    }
  }

  def indexMessageSentReq(req: MessageSent): Future[Unit] = indexMessageSentReqs(Seq(req))

  def indexMessageSentReqs(reqs: Seq[MessageSent]): Future[Unit] = {
    val writeReqs: Seq[IndexRequest] = reqs.map { req =>
      import req._
      val xContent: XContentBuilder = XContentFactory.jsonBuilder().startObject()
        .field(ESFieldName.activity_id, activityId)
        .field(ESFieldName.usercode, usercode.string)
        .field(ESFieldName.output, output.name)
        .field(ESFieldName.state, state.dbValue)
        .endObject()
      val indexName = s"${helper.deliveryReportIndexName}${helper.dateSuffixString()}"
      helper.makeIndexRequest(indexName, helper.deliveryReportDocumentType, s"$activityId:${usercode.string}:${output.name}", xContent)
    }
    makeBulkRequest(writeReqs)
  }

  def indexActivityReqs(reqs: Seq[IndexActivityRequest]): Future[Unit] = {
    val writeReqs: Seq[IndexRequest] = reqs.map { req =>
      val activity = req.activity
      val resolvedUsers = req.resolvedUsers
      val activityDocument = ActivityDocument.fromActivityModel(
        activity,
        audienceService,
        publisherService,
        resolvedUsers
      )
      val docBuilder = helper.elasticSearchContentBuilderFromActivityDocument(activityDocument)

      val indexName = helper.indexNameForActivity(activity)

      helper.makeIndexRequest(indexName, helper.activityDocumentType, activity.id, docBuilder)
    }
    makeBulkRequest(writeReqs)
  }

  private def handleDeliveryReportResponse(searchResponse: SearchResponse): AlertDeliveryReport = {
    Option(searchResponse.getAggregations) match {
      case Some(aggregations: Aggregations) =>
        // did the query actually match anything, cardinality will still exist as 0 if we didn't
        if (searchResponse.getHits.getTotalHits > 0) {
          val cardinality: Cardinality = aggregations.get(ESFieldName.distinct_users_agg)
          AlertDeliveryReport(Some(cardinality.getValue.toInt))
        } else AlertDeliveryReport.empty
      case _ => AlertDeliveryReport.empty
    }
  }

  override def deliveryReportForActivity(activityId: String, publishedAt: Option[DateTime]): Future[AlertDeliveryReport] =
    publishedAt.map { date =>
      import ESFieldName._
      val deliveryReportIndex = s"${helper.deliveryReportIndexName}${helper.dateSuffixString(date)}"
      val searchRequest: SearchRequest = new SearchRequest(deliveryReportIndex).types(helper.deliveryReportDocumentType)
      searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen())
      searchRequest.source(
        new SearchSourceBuilder().size(0)
          .query(QueryBuilders.boolQuery()
            .must(QueryBuilders.termsQuery(state_keyword, MessageState.Success.dbValue, MessageState.Muted.dbValue))
            .must(QueryBuilders.termQuery(activity_id_keyword, activityId)))
          .aggregation(AggregationBuilders.cardinality(distinct_users_agg)
            .field(usercode_keyword)
            .precisionThreshold(40000) // 40000 is max precision threshold (https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-cardinality-aggregation.html#CO67-1)
          )
      )
      val listener = new FutureActionListener[SearchResponse]
      client.searchAsync(searchRequest, listener)
      listener.future.map(response =>
        if (response.status() == RestStatus.OK) {
          handleDeliveryReportResponse(response)
        } else {
          logger.error(s"ES activity delivery report query responded with status: ${response.status().getStatus}")
          AlertDeliveryReport.empty
        }).recover {
        case e: Throwable =>
          logger.error(s"ES request for delivery report failed", e)
          AlertDeliveryReport.empty
      }
    }.getOrElse {
      logger.debug("Unable to query delivery report for activity when publishedAt is None")
      Future.successful(AlertDeliveryReport.empty)
    }

  override def count(input: ActivityESSearch.SearchQuery): Future[ActivityESSearch.CountQueryResponse] = {
    val searchRequest: SearchRequest = new SearchRequest(ActivityESServiceSearchHelper.indexNameForActivitySearchQuery(input))
      .types(ActivityESServiceHelper.activityDocumentType)
      .source(new SearchSourceBuilder()
        .size(0)
        .query(ActivityESServiceSearchHelper.makeBoolQueryBuilder(input))
        .aggregation(ActivityESServiceCountHelper.Aggregation.TotalUserCount.builder)
      )

    val listener = new FutureActionListener[SearchResponse]
    client.searchAsync(searchRequest, listener)
    listener.future.map { response =>
      if (response.status() == RestStatus.OK) {
        CountQueryResponse(
          activityCount = response.getHits.totalHits,
          totalUserCount = Try {
            response.getAggregations
              .get(ActivityESServiceCountHelper.Aggregation.TotalUserCount.fieldName)
              .asInstanceOf[Sum]
              .getValue
              .toLong
          }.recover{
            case error =>
              logger.error(s"Exception thrown when getting aggregation value", error)
              0L
          }.getOrElse(0L)
        )
      } else {
        CountQueryResponse.empty
      }
    }.recover {
      case e: Throwable =>
        logger.error(s"ES request for activities counts failed", e)
        CountQueryResponse.empty
    }
  }
}