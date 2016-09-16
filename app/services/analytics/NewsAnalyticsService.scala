package services.analytics

import com.google.api.services.analyticsreporting.v4.model._
import com.google.inject.{ImplementedBy, Inject}

import scala.collection.JavaConverters._

case class AnalyticsReport(id: String, metric: String)

object AnalyticsReport {
  def fromGaReport(report: Report): Seq[AnalyticsReport] = {
    report.getData.getRows.asScala.map { row =>
      AnalyticsReport(
        row.getDimensions.get(0),
        row.getMetrics.get(0).getValues.get(0)
      )
    }
  }
}

@ImplementedBy(classOf[NewsAnalyticsServiceImpl])
trait NewsAnalyticsService {
  def getClicks(ids: Seq[String]): Seq[AnalyticsReport]
}

class NewsAnalyticsServiceImpl @Inject()(
  analytics: AnalyticsReportService
) extends NewsAnalyticsService {

  override def getClicks(ids: Seq[String]): Seq[AnalyticsReport] = {
    val report = analytics.getReport(ids, Seq("uniqueEvents"), Seq("eventLabel"))
    report.getReports.asScala.map(AnalyticsReport.fromGaReport).head
  }

}

