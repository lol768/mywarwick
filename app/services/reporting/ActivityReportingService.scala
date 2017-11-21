package services.reporting

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.ActivityProvider
import org.joda.time.Interval
import play.api.db.{Database, NamedDatabase}
import services.ProviderRender
import services.dao.PublisherDao
import services.elasticsearch.{ActivityDocument, ActivityESSearchQuery, ActivityESService}
import scala.concurrent.Future

@ImplementedBy(classOf[ActivityReportingServiceImpl])
trait ActivityReportingService {
  def alertsByProvider(provider: ActivityProvider, interval: Interval): Future[Seq[ActivityDocument]]

  def alertsCountByProvider(provider: ActivityProvider, interval: Interval): Future[Int]

  def alertsByProviders(providers: Map[ActivityProvider, Interval]): Future[Iterable[(ActivityProvider, Seq[ActivityDocument])]]

  def alertsCountByProviders(providers: Map[ActivityProvider, Interval]): Future[Iterable[(ActivityProvider, Int)]]

  def allAlertsByProviders(interval: Interval): Future[Iterable[(ActivityProvider, Seq[ActivityDocument])]]

  def allAlertsCountByProviders(interval: Interval): Future[Iterable[(ActivityProvider, Int)]]

  // activity as in non-alert activity
  def activitiesByProvider(provider: ActivityProvider, interval: Interval): Seq[ActivityDocument]

  def activitiesByProviders(providers: Map[ActivityProvider, Interval]): Future[Iterable[(ActivityProvider, Seq[ActivityDocument])]]
}

class ActivityReportingServiceImpl @Inject()(
  activityESService: ActivityESService,
  publisherDao: PublisherDao,
  @NamedDatabase("default") db: Database
) extends ActivityReportingService {
  override def alertsByProvider(provider: ActivityProvider, interval: Interval): Future[Seq[ActivityDocument]] = {
    val query = ActivityESSearchQuery(
      provider_id = Some(provider.id),
      publish_at = Some(interval),
      isAlert = Some(true)
    )
    activityESService.search(query)
  }

  override def alertsCountByProvider(provider: ActivityProvider, interval: Interval) = {
    val query = ActivityESSearchQuery(
      provider_id = Some(provider.id),
      publish_at = Some(interval),
      isAlert = Some(true)
    )
    activityESService.count(query)
  }

  override def alertsByProviders(providers: Map[ActivityProvider, Interval]): Future[Iterable[(ActivityProvider, Seq[ActivityDocument])]] = {
    import system.ThreadPools.elastic
    Future.sequence(providers.map {
      case (provider, interval) => (provider, this.alertsByProvider(provider, interval))
    }.map {
      case (provider, futureActivityDocuments) =>
        for {
          activityDocuments <- futureActivityDocuments
        } yield (provider, activityDocuments)
    }).map { result =>
      result.toSeq.sortBy {
        case (provider, _) => provider.displayName.getOrElse(provider.id)
      }
    }
  }

  override def alertsCountByProviders(providers: Map[ActivityProvider, Interval]) = {
    import system.ThreadPools.elastic
    Future.sequence(providers.map {
      case (provider, interval) => (provider, this.alertsCountByProvider(provider, interval))
    }.map {
      case (provider, futureCount) =>
        for {
          count <- futureCount
        } yield (provider, count)
    }).map { result =>
      result.toSeq.sortBy {
        case (provider, _) => provider.displayName.getOrElse(provider.id)
      }
    }
  }


  override def allAlertsByProviders(interval: Interval): Future[Iterable[(ActivityProvider, Seq[ActivityDocument])]] = {
    db.withConnection(implicit c => {
      val providersWithInterval = publisherDao
        .getAllProviders()
        .map(ProviderRender.toActivityProvider)
        .map((_, interval))
        .toMap
      this.alertsByProviders(providersWithInterval)
    })
  }

  override def allAlertsCountByProviders(interval: Interval) = {
    db.withConnection(implicit c => {
      val providersWithInterval = publisherDao
        .getAllProviders()
        .map(ProviderRender.toActivityProvider)
        .map((_, interval))
        .toMap
      this.alertsCountByProviders(providersWithInterval)
    })
  }

  override def activitiesByProvider(provider: ActivityProvider, interval: Interval) = ???

  override def activitiesByProviders(providers: Map[ActivityProvider, Interval]) = ???
}
