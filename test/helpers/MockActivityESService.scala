package helpers

import org.joda.time.DateTime
import services.elasticsearch._

import scala.concurrent.Future

class MockActivityESService extends ActivityESService {
  override def indexActivityReq(req: IndexActivityRequest): Future[Unit] = Future.successful({})
  override def indexActivityReqs(requests: Seq[IndexActivityRequest]): Future[Unit] = Future.successful({})
  override def indexMessageSentReq(req: MessageSent): Future[Unit] = Future.successful({})
  override def indexMessageSentReqs(reqs: Seq[MessageSent]): Future[Unit] = Future.successful({})
  override def count(activityESSearchQuery: ActivityESSearch.SearchQuery): Future[ActivityESSearch.CountQueryResponse] = Future.successful(ActivityESSearch.CountQueryResponse(0L, 0L))
  override def deliveryReportForActivity(activityId: String, publishedAt: Option[DateTime]): Future[AlertDeliveryReport] = Future.successful(AlertDeliveryReport(None))
}
