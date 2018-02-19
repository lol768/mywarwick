package services.elasticsearch

import javax.inject.{Inject, Singleton}
import javax.ws.rs.HttpMethod

import com.google.inject.ImplementedBy
import models.{Activity, MessageState, Output}
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.{Response, ResponseException, RestClient, RestHighLevelClient}
import org.elasticsearch.common.xcontent.{XContentBuilder, XContentFactory}
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import services.elasticsearch.ActivityESServiceHelper.ESFieldName
import services.{AudienceService, PublisherService}
import system.ThreadPools.elastic
import warwick.core.Logging
import warwick.sso.Usercode

import scala.concurrent.Future

case class IndexActivityRequest(activity: Activity, resolvedUsers: Option[Seq[Usercode]] = None)
case class MessageSent(activityId: String, usercode: Usercode, state: MessageState, output: Output)

object MessageSent {
  implicit val reads: Reads[MessageSent] = (
    (JsPath \ "_source" \ "activity_id").read[String] ~
      (JsPath \ "_source" \ "usercode").read[String].map(Usercode) ~
      (JsPath \ "_source" \ "state").read[String].map(s => MessageState.parse(s)) ~
      (JsPath \ "_source" \ "output").read[String].map(s => Output.parse(s))
    )(MessageSent.apply _)
}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  val helper = ActivityESServiceIndexHelper

  def indexActivityReq(req: IndexActivityRequest): Future[Unit]

  def indexActivityReqs(requests: Seq[IndexActivityRequest]): Future[Unit]

  def indexMessageSentReq(req: MessageSent): Future[Unit]

  def indexMessageSentReqs(reqs: Seq[MessageSent]): Future[Unit]

  def count(activityESSearchQuery: ActivityESSearchQuery): Future[Int]

  def messageSentDetailsForActivity(activityId: String, publishedAt: Option[DateTime]): Future[Option[MessageSentDetails]]
}

@Singleton
class ActivityESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  audienceService: AudienceService,
  publisherService: PublisherService,
  elasticSearchAdminService: ElasticSearchAdminService
) extends ActivityESService with Logging {

  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.activityEsTemplates, "activity_template_default")
  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.alertEsTemplates, "alert_template_default")
  elasticSearchAdminService.putTemplate(ActivityESServiceIndexHelper.messageSendEsTemplates, "message_send_template_default")

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
        .field("activity_id", activityId)
        .field("usercode", usercode.string)
        .field("output", output.name)
        .field("state", state.dbValue)
        .endObject()
      val indexName = s"${helper.messageSendIndexName}${helper.dateSuffixString()}"
      helper.makeIndexRequest(indexName, helper.messageSendDocumentType, s"$activityId:${usercode.string}:${output.name}", xContent)
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

  private def buildSentDetails(seq: Seq[MessageSent]): SentDetails = {
    import Output._
    val groupedByOutput = seq.groupBy(_.output)
    SentDetails(
      sms = groupedByOutput.getOrElse(SMS, Nil).map(_.usercode),
      email = groupedByOutput.getOrElse(Email, Nil).map(_.usercode),
      mobile = groupedByOutput.getOrElse(Mobile, Nil).map(_.usercode)
    )
  }

  private def handleMessageSentDetailsResponse(res: Response): Option[MessageSentDetails] = {
    val json = Json.parse(scala.io.Source.fromInputStream(res.getEntity.getContent).mkString)
    (json \ "hits" \ "hits").validate[Seq[MessageSent]].fold(
      invalid => {
        logger.error(s"Could not parse JSON in Elastic Search response:\\n${Json.prettyPrint(json)}")
        invalid.foreach { case (path, errs) => logger.error(s"$path: ${errs.map(_.message).mkString(", ")}") }
        None
      },
      valid => valid match {
        case messageSents if messageSents.nonEmpty =>
          val groupedByState = messageSents.par.groupBy(_.state)
          import MessageState._
          Some(MessageSentDetails(
            successful = buildSentDetails(groupedByState.getOrElse(Success, Nil).seq),
            failed = buildSentDetails(groupedByState.getOrElse(Failure, Nil).seq),
            skipped = buildSentDetails(groupedByState.getOrElse(Skipped, Nil).seq)
          ))
        case _ =>
          logger.debug(s"MessageSent data not found in Elastic Search json response:\n${Json.prettyPrint(json)}")
          None
      }
    )
  }

  override def messageSentDetailsForActivity(activityId: String, publishedAt: Option[DateTime]): Future[Option[MessageSentDetails]] =
    publishedAt.map { date =>
      val query = Json.obj(
        "query" -> Json.obj(
          "term" -> Json.obj(
            ESFieldName.activity_id_keyword -> activityId
          )
        )
      )

      LowLevelClientHelper.performRequestAsync(
        method = HttpMethod.GET,
        path = s"/${helper.messageSendIndexName}${helper.dateSuffixString(date)}/_search",
        entity = Some(new NStringEntity(query.toString, ContentType.APPLICATION_JSON)),
        lowLevelClient = lowLevelClient
      ).map(handleMessageSentDetailsResponse).recover {
        case _ => None // exception is logged by es client
      }
    }.getOrElse(Future(None))

  override def count(input: ActivityESSearchQuery): Future[Int] = {
    val lowHelper = LowLevelClientHelper
    lowHelper.performRequestAsync(
      method = HttpMethod.GET,
      path = lowHelper.makePathForCountApiFromActivityEsSearchQuery(input),
      entity = Some(lowHelper.httpEntityFromJsValue(lowHelper.makeQueryForCountApiFromActivityESSearchQuery(input))),
      lowLevelClient = lowLevelClient
    ).map {
      lowHelper.getCountFromCountApiRes
    }
  }
}