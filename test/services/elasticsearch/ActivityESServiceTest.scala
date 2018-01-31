package services.elasticsearch

import java.util
import javax.ws.rs.HttpMethod

import helpers.{BaseSpec, MinimalAppPerSuite}
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.{ResponseListener, RestClient}
import org.joda.time.DateTime
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.routing.Router.Routes
import play.api.routing.sird._
import services.{AudienceService, PublisherService}
import warwick.sso.Usercode

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ActivityESServiceTest extends BaseSpec with MockitoSugar with BaseController with MinimalAppPerSuite {

  val controllerComponents: ControllerComponents = get[ControllerComponents]

  val audienceService: AudienceService = mock[AudienceService]
  val publisherService: PublisherService = mock[PublisherService]
  val elasticSearchAdminService: ElasticSearchAdminService = mock[ElasticSearchAdminService]

  "send correct request to Elastic Search for message send data" in {
    val eSClientConfig: ESClientConfig = mock[ESClientConfig]
    val restClient: RestClient = mock[RestClient]

    when(eSClientConfig.lowLevelClient).thenReturn(restClient)

    val activityESService: ActivityESService = new ActivityESServiceImpl(
      eSClientConfig,
      audienceService,
      publisherService,
      elasticSearchAdminService
    )

    doNothing().when(restClient).performRequestAsync(any[String], any[String], any[util.Map[String, String]], any[HttpEntity], any[ResponseListener])

    val activityId = "777"

    activityESService.messageSentDetailsForActivity(activityId, Some(DateTime.now))

    val methodCaptor = ArgumentCaptor.forClass(classOf[String])
    val pathCaptor = ArgumentCaptor.forClass(classOf[String])
    val paramsCaptor = ArgumentCaptor.forClass(classOf[util.Map[String, String]])
    val entityCaptor = ArgumentCaptor.forClass(classOf[HttpEntity])
    val listenerCaptor = ArgumentCaptor.forClass(classOf[ResponseListener])

    verify(restClient).performRequestAsync(methodCaptor.capture, pathCaptor.capture, paramsCaptor.capture, entityCaptor.capture, listenerCaptor.capture)

    val expectedMethod: String = HttpMethod.GET
    val expectedEndpoint: String = s"/message_send_${DateTime.now.toString("yyyy_MM")}/_search"
    val expectedQuery: JsValue = Json.parse(s"""{"query": {"match": {"activity_id" : "$activityId" }}}""")
    val expectedEntity: NStringEntity = new NStringEntity(expectedQuery.toString, ContentType.APPLICATION_JSON)

    methodCaptor.getValue must equal(expectedMethod)
    pathCaptor.getValue must equal(expectedEndpoint)
    // get NStringEntity in a comparable type int
    entityCaptor.getValue.getContent.read must equal(expectedEntity.getContent.read)
  }

  "build MessageSentDetails from ES Response" in {
    val activityId: String = "212"
    val responseJson: JsValue = Json.parse(
      s"""
           {
             "hits" : {
                "hits" : [
                   {
                      "_source" : {
                         "usercode" : "cusjau",
                         "state" : "F",
                         "activity_id" : "$activityId",
                         "output" : "mobile"
                      },
                      "_score" : 1,
                      "_id" : "$activityId:cusjau:mobile",
                      "_index" : "message_send",
                      "_type" : "message_send"
                   }
                 ]
               }
          }
          """
    )

    val routes: Routes = {
      case GET(p"/message_send_$date/_search") =>
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

      val actual: Future[Option[MessageSentDetails]] = activityESService.messageSentDetailsForActivity(activityId, Some(DateTime.now))

      def sentDetails(sms: Seq[Usercode] = Nil, email: Seq[Usercode] = Nil, mobile: Seq[Usercode] = Nil) = SentDetails(sms, email, mobile)

      val expected: Option[MessageSentDetails] =
        Some(MessageSentDetails(
          successful = sentDetails(),
          failed = sentDetails(mobile = Seq(Usercode("cusjau"))),
          skipped = sentDetails()
        ))

      val result = Await.result(actual, 1.second)
      result mustBe expected
    }
  }
}
