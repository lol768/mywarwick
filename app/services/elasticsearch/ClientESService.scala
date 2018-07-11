package services.elasticsearch

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named, Singleton}
import org.elasticsearch.action.search.{ClearScrollRequest, SearchRequest, SearchScrollRequest}
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.Scroll
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.{Duration, Interval}
import services.reporting.UserAccess
import warwick.core.Logging

import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[ClientESServiceImpl])
trait ClientESService {
  def available: Boolean
  def fetchAccesses(interval: Interval): ClientAccessData
}

@Singleton
class ClientESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  elasticSearchAdminService: ElasticSearchAdminService
)(implicit @Named("elastic") ec: ExecutionContext) extends ClientESService with Logging {

  private val client: RestHighLevelClient = eSClientConfig.clogsClient
  private val oneMinute = TimeValue.timeValueMinutes(1L)
  private val scrollSize = 5000
    
  def available: Boolean = try {
    client.ping()
  } catch {
    case e: Exception =>
      logger.error("Exception testing CLogS availability", e)
      false
  }
  
  private def clearScroll(scrollId: Option[String]): Unit = {
    // Can't currently DELETE on CLogS as it's firewalled, so just allow the scrolls to expire naturally
    return 
    
    scrollId.map { id =>
      val clearScrollRequest = new ClearScrollRequest
      clearScrollRequest.addScrollId(id)
      client.clearScroll(clearScrollRequest)
    }
  }
  
  private def buildIndexName(interval: Interval): String = {
    Iterator.iterate(interval.getStart) {
      _.plusDays(1)
    }.takeWhile(!_.isAfter(interval.getEnd))
      .map(d => s"web-development-access-${d.getYear}.${"%02d".format(d.getMonthOfYear)}.${"%02d".format(d.getDayOfMonth)}")
      .toSeq
      .mkString(",")
  }

  private def buildAccountLookupQuery(interval: Interval): BoolQueryBuilder = {
    QueryBuilders.boolQuery
      .must(QueryBuilders.termQuery("node_deployment", "prod"))
      .must(QueryBuilders.termQuery("node_app", "mywarwick"))
      .must(QueryBuilders.termQuery("requested_uri", "/api/tiles/content/account"))
      .must(QueryBuilders.rangeQuery("@timestamp").from(interval.getStart.getMillis).to(interval.getEnd.getMillis))
  }

  def fetchAccesses(interval: Interval): ClientAccessData = {
    var results = Seq.empty[UserAccess]
    val startTime = System.currentTimeMillis
 
    val searchRequest = new SearchRequest(buildIndexName(interval))
    val searchSourceBuilder = new SearchSourceBuilder
    val scroll = new Scroll(oneMinute)
    searchSourceBuilder.query(buildAccountLookupQuery(interval))
    searchSourceBuilder.size(scrollSize)
    searchSourceBuilder.timeout(oneMinute)
    searchSourceBuilder.fetchSource(Array[String]("username", "user-agent-detail.device", "user-agent-detail.os", "request_headers.user-agent"), null)
    searchRequest.source(searchSourceBuilder)
    searchRequest.scroll(scroll)
    
    val response = client.search(searchRequest)
    if (response.status == RestStatus.OK) {
      logger.debug(s"CLogS has ${response.getHits.totalHits} hits\nFirst lookup (hits 1 - ${scrollSize - 1}")
      var scrollId = Option(response.getScrollId)
      var searchHits = Option(response.getHits.getHits).getOrElse(Array.empty)
      
      var round: Int = 0
      
      while (searchHits.nonEmpty) {
        results ++= searchHits.map(UserAccess.fromESSearchHit)
        round += 1;
        logger.debug(s"Scroll $round (hits ${round * scrollSize}-${(round + 1) * scrollSize - 1})")

        val scrollRequest = new SearchScrollRequest(scrollId.getOrElse(""))
        scrollRequest.scroll(scroll)
        val searchResponse = client.searchScroll(scrollRequest)
        scrollId = Option(searchResponse.getScrollId)
        searchHits = Option(searchResponse.getHits.getHits).getOrElse(Array.empty)
      }

      if (searchHits.nonEmpty) {
        results ++= searchHits.map(UserAccess.fromESSearchHit)
      }

      clearScroll(scrollId)
      val endTime = System.currentTimeMillis
      val requestTime = new Duration(endTime - startTime)
      ClientAccessData(requestTime, results)
    } else {
      logger.warn(s"CLogS request for client statistical data returned unexpected status ${response.status.getStatus}")
      ClientAccessData(new Duration(0), Seq.empty)
    }
  }
}

case class ClientAccessData(val duration: Duration, val accesses: Seq[UserAccess])
