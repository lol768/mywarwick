package services.elasticsearch

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named, Singleton}
import org.elasticsearch.action.search.{ClearScrollRequest, SearchRequest, SearchResponse, SearchScrollRequest}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.{Scroll, SearchHit}
import org.joda.time.Interval
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import services.reporting.UserAccess
import warwick.core.Logging

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ClientESServiceImpl])
trait ClientESService {
  def available: Boolean
  def fetchAccesses(interval: Interval): Future[Seq[UserAccess]]
}

@Singleton
class ClientESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  elasticSearchAdminService: ElasticSearchAdminService
)(implicit @Named("elastic") ec: ExecutionContext) extends ClientESService with Logging {

  private val client: RestHighLevelClient = eSClientConfig.clogsClient
  private val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  private val oneMinute = TimeValue.timeValueMinutes(1L)
  
  private def parseAccessHit(sh: SearchHit): UserAccess = UserAccess(
    sh.field("username").getValue[String],
    sh.field("user-agent-detail.device").getValue[String],
    sh.field("user-agent-detail.os").getValue[String],
    sh.field("request_headers.user-agent").getValue[String]
  )
  
  private def buildAccountLookupQuery(interval: Interval): BoolQueryBuilder = {
    QueryBuilders.boolQuery
      // TODO: better to derive deployment from conf
      .must(QueryBuilders.termQuery("node_deployment", "prod"))
      .must(QueryBuilders.termQuery("node_app", "mywarwick"))
      .must(QueryBuilders.termQuery("requested_uri", "/api/tiles/content/account"))
      .must(QueryBuilders.rangeQuery("@timestamp").from(s"${fmt.print(interval.getStart)}||/D").to(s"${fmt.print(interval.getEnd)}||/D"))
  }
  
  def available: Boolean = try {
    client.ping()
  } catch {
    case e: Exception => {
      logger.error("Exception testing ES availability", e)
      false
    }
  }
  
  def fetchAccesses(interval: Interval): Future[Seq[UserAccess]] = {
    var results = Seq.empty[UserAccess]
    
    val searchRequest = new SearchRequest("web-development-*")
    val searchSourceBuilder = new SearchSourceBuilder
    val scroll = new Scroll(oneMinute)
    searchSourceBuilder.query(buildAccountLookupQuery(interval))
    searchSourceBuilder.size(500)
    searchSourceBuilder.timeout(oneMinute)
    searchSourceBuilder.fetchSource(Array[String]("username", "user-agent-detail.device", "user-agent-detail.os", "request_headers.user-agent"), null)
    searchRequest.source(searchSourceBuilder)
    searchRequest.scroll(scroll)
    
    val listener = new FutureActionListener[SearchResponse]
    client.searchAsync(searchRequest, listener)
    listener.future.map { response =>
      if (response.status == RestStatus.OK) {
        var scrollId = Option(response.getScrollId)
        var searchHits = Option(response.getHits.getHits).getOrElse(Array.empty)
        
        while (searchHits.nonEmpty) {
          results ++= searchHits.map(parseAccessHit)

          val scrollRequest = new SearchScrollRequest(scrollId.getOrElse(""))
          scrollRequest.scroll(scroll)
          val searchResponse = client.searchScroll(scrollRequest)
          scrollId = Option(searchResponse.getScrollId)
          searchHits = Option(searchResponse.getHits.getHits).getOrElse(Array.empty)
        }

        if (searchHits.nonEmpty) {
          results ++= searchHits.map(parseAccessHit)
        }

        scrollId.map { id =>
          val clearScrollRequest = new ClearScrollRequest
          clearScrollRequest.addScrollId(id)
          client.clearScroll(clearScrollRequest)
        }
        
        results
      } else {
        logger.warn(s"ES request for client statistical data returned unexpected status ${response.status.getStatus}")
        Seq.empty
      }
    }.recover {
      case e: Throwable =>
        logger.error("ES request for client statistical data failed", e)
        Seq.empty
    }
  }
}
