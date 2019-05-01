package services.elasticsearch

import helpers.{BaseSpec, MinimalAppPerSuite}
import models.MessageState
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
import org.elasticsearch.index.query.{BoolQueryBuilder, TermQueryBuilder, TermsQueryBuilder}
import org.joda.time.DateTime
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.routing.Router.Routes
import play.api.routing.sird._
import services.elasticsearch.ActivityESServiceHelper.ESFieldName
import services.{AudienceService, PublisherService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ActivityESServiceTest extends BaseSpec with MockitoSugar with BaseController with MinimalAppPerSuite {

  val controllerComponents: ControllerComponents = get[ControllerComponents]

  val audienceService: AudienceService = mock[AudienceService]
  val publisherService: PublisherService = mock[PublisherService]
  val elasticSearchAdminService: ElasticSearchAdminService = mock[ElasticSearchAdminService]

  "send correct request to Elastic Search for message send data" in {
    val eSClientConfig: ESClientConfig = mock[ESClientConfig]
    val restClient: RestHighLevelClient = mock[RestHighLevelClient](RETURNS_SMART_NULLS)

    when(eSClientConfig.highLevelClient).thenReturn(restClient)

    val activityESService: ActivityESService = new ActivityESServiceImpl(
      eSClientConfig,
      audienceService,
      publisherService,
      elasticSearchAdminService
    )

    val activityId = "777"
    activityESService.deliveryReportForActivity(activityId, Some(DateTime.now))
    val searchRequestCaptor = ArgumentCaptor.forClass(classOf[SearchRequest])
    verify(restClient).searchAsync(searchRequestCaptor.capture, any[RequestOptions], any[FutureActionListener[SearchResponse]])

    val builder: BoolQueryBuilder = searchRequestCaptor.getValue.source.query().asInstanceOf[BoolQueryBuilder]
    val termsQueryOne: TermsQueryBuilder = builder.must().get(0).asInstanceOf[TermsQueryBuilder]
    val termQueryTwo: TermQueryBuilder = builder.must().get(1).asInstanceOf[TermQueryBuilder]

    termsQueryOne.fieldName() must equal (ESFieldName.state_keyword)
    termsQueryOne.values().get(0).asInstanceOf[String] must equal (MessageState.Success.dbValue)
    termsQueryOne.values().get(1).asInstanceOf[String] must equal (MessageState.Muted.dbValue)

    termQueryTwo.fieldName() must equal (ESFieldName.activity_id_keyword)
    termQueryTwo.value().asInstanceOf[String] must equal (activityId)
  }

  "build AlertDeliveryReport from ES Response" in {
    val activityId: String = "212"
    val responseJson: JsValue = Json.parse(
      s"""
         {
           "took": 6,
           "timed_out": false,
           "_shards": {
             "total": 5,
             "successful": 5,
             "skipped": 0,
             "failed": 0
           },
           "hits": {
             "total": 99,
             "max_score": null,
             "hits": []
           },
           "aggregations": {
             "cardinality#distinct_users": {
               "value": 99
             }
           }
         }
          """
    )

    val routes: Routes = {
      case POST(p"/delivery_report_$date/delivery_report/_search") => // / index_name / document_type / api
        Action(Ok(responseJson))
    }

    MockElastic.running(routes) { info =>
      val config = new ESClientConfigImpl(Configuration(
        "es.nodes" -> Seq(s"localhost:${info.port}")
      ))

      val activityESService: ActivityESService = new ActivityESServiceImpl(
        config,
        audienceService,
        publisherService,
        elasticSearchAdminService
      )

      val actual: Future[AlertDeliveryReport] = activityESService.deliveryReportForActivity(activityId, Some(DateTime.now))

      val expected: AlertDeliveryReport = AlertDeliveryReport(Some(99))

      val result = Await.result(actual, 1.second)
      result mustBe expected
    }
  }
}
