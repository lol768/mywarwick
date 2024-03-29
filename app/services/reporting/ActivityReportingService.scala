package services.reporting

import javax.inject.{Inject, Named}
import com.google.inject.ImplementedBy
import models.ActivityProvider
import org.joda.time.Interval
import play.api.db.{Database, NamedDatabase}
import services.ProviderRender
import services.dao.PublisherDao
import services.elasticsearch.ActivityESSearch.CountQueryResponse
import services.elasticsearch.{ActivityESSearch, ActivityESService}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ActivityReportingServiceImpl])
trait ActivityReportingService {
  type ProviderCounts = Seq[(ActivityProvider, CountQueryResponse)]

  def alertsCountByProvider(provider: ActivityProvider, interval: Interval): Future[CountQueryResponse]

  def alertsCountByProviders(providers: Map[ActivityProvider, Interval]): Future[ProviderCounts]

  def allAlertsCountByProviders(interval: Interval): Future[ProviderCounts]

  // We are not using the search function at all at the moment, but if one day we do,
  // anything that could return a lot of results should use pagination, implemented
  // using the search_after feature
  // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-search-after.html
}

class ActivityReportingServiceImpl @Inject()(
  activityESService: ActivityESService,
  publisherDao: PublisherDao,
  @NamedDatabase("default") db: Database
)(implicit @Named("elastic") ec: ExecutionContext) extends ActivityReportingService {

  override def alertsCountByProvider(provider: ActivityProvider, interval: Interval): Future[CountQueryResponse] = {
    val query = ActivityESSearch.SearchQuery(
      provider_id = Some(provider.id),
      publish_at = Some(interval),
      isAlert = Some(true)
    )
    activityESService.count(query)
  }

  override def alertsCountByProviders(providers: Map[ActivityProvider, Interval]): Future[ProviderCounts] = {
    Future.sequence(providers.map {
      case (provider, interval) => (provider, this.alertsCountByProvider(provider, interval))
    }.map {
      case (provider, eventualCountQueryResponse) =>
        for {
          countQueryResponse <- eventualCountQueryResponse
        } yield (provider, countQueryResponse)
    }).map { result =>
      result.toSeq.sortBy {
        case (provider, _) => provider.displayName.getOrElse(provider.id)
      }
    }
  }

  override def allAlertsCountByProviders(interval: Interval): Future[ProviderCounts] = {
    db.withConnection(implicit c => {
      val providersWithInterval = publisherDao
        .getAllProviders()
        .map(ProviderRender.toActivityProvider)
        .map((_, interval))
        .toMap
      this.alertsCountByProviders(providersWithInterval)
    })
  }
}
