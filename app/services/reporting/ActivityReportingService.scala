package services.reporting

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.ActivityProvider
import org.joda.time.Interval
import play.api.db.{Database, NamedDatabase}
import services.ProviderRender
import services.dao.PublisherDao
import services.elasticsearch.{ActivityDocument, ActivityESSearchQuery, ActivityESService}

import scala.collection.immutable.ListMap
import scala.concurrent.Future

@ImplementedBy(classOf[ActivityReportingServiceImpl])
trait ActivityReportingService {
  def alertsByProvider(provider: ActivityProvider, interval: Interval): Future[Seq[ActivityDocument]]

  def alertsByProviders(providers: Map[ActivityProvider, Interval]): Future[Map[ActivityProvider, Seq[ActivityDocument]]]

  def allAlertsByProviders(interval: Interval): Future[Map[ActivityProvider, Seq[ActivityDocument]]]

  // activity as in non-alert activity
  def activitiesByProvider(provider: ActivityProvider, interval: Interval): Seq[ActivityDocument]

  def activitiesByProviders(providers: Map[ActivityProvider, Interval]): Future[Map[ActivityProvider, Seq[ActivityDocument]]]
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

  override def alertsByProviders(providers: Map[ActivityProvider, Interval]): Future[Map[ActivityProvider, Seq[ActivityDocument]]] = {
    import system.ThreadPools.elastic
    Future.sequence(providers.map {
      case (provider, interval) => (provider, this.alertsByProvider(provider, interval))
    }.map {
      case (provider, futureActivityDocuments) =>
        for {
          activityDocuments <- futureActivityDocuments
        } yield (provider, activityDocuments)
    }).map(result => {
      ListMap(result.toSeq.sortBy {
        case (provider, _) => provider.displayName.getOrElse(provider.id)
      }: _*)
    })
  }


  override def activitiesByProvider(provider: ActivityProvider, interval: Interval) = ???

  override def activitiesByProviders(providers: Map[ActivityProvider, Interval]) = ???

  override def allAlertsByProviders(interval: Interval): Future[Map[ActivityProvider, Seq[ActivityDocument]]] = {
    db.withConnection(implicit c => {
      val providersWithInterval = publisherDao
        .getAllProviders()
        .map(ProviderRender.toActivityProvider)
        .map((_, interval))
        .toMap
      this.alertsByProviders(providersWithInterval)
    })
  }
}
