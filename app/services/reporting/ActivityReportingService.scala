package services.reporting

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.ActivityProvider
import org.joda.time.Interval
import services.elasticsearch.{ActivityDocument, ActivityESSearchQuery, ActivityESService}

import scala.concurrent.Future

@ImplementedBy(classOf[ActivityReportingServiceImpl])
trait ActivityReportingService {
  def alertsByProvider(provider: ActivityProvider, interval: Interval): Future[Seq[ActivityDocument]]

  def alertsByProviders(providers: Map[ActivityProvider, Interval]): Map[ActivityProvider, Future[Seq[ActivityDocument]]]

  // activity as in non-alert activity
  def activitiesByProvider(provider: ActivityProvider, interval: Interval): Seq[ActivityDocument]

  def activitiesByProviders(providers: Map[ActivityProvider, Interval]): Map[ActivityProvider, Future[Seq[ActivityDocument]]]
}

class ActivityReportingServiceImpl @Inject()(
  activityESService: ActivityESService
) extends ActivityReportingService {
  override def alertsByProvider(provider: ActivityProvider, interval: Interval) = {
    val query = ActivityESSearchQuery(
      provider_id = Some(provider.id),
      publish_at = Some(interval),
      isAlert = Some(true)
    )
    activityESService.search(query)
  }

  override def alertsByProviders(providers: Map[ActivityProvider, Interval]) = providers.map {
    case (provider, interval) => (provider, this.alertsByProvider(provider, interval))
  }

  override def activitiesByProvider(provider: ActivityProvider, interval: Interval) = ???

  override def activitiesByProviders(providers: Map[ActivityProvider, Interval]) = ???
}
