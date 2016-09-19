package services.analytics

import com.google.api.services.analyticsreporting.v4.model._
import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.json._

import scala.collection.JavaConverters._

case class AnalyticsReport(id: String, metric: String)

object AnalyticsReport {
  def fromGaReport(row: ReportRow): AnalyticsReport = {
    AnalyticsReport(
      row.getDimensions.get(0),
      row.getMetrics.get(0).getValues.get(0)
    )
  }

  implicit val writes = new Writes[AnalyticsReport] {
    override def writes(o: AnalyticsReport): JsValue =
      Json.obj(o.id -> o.metric)
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
    // tried to do single filter and have setExpressions(List(ids)) but apparently that doesn't work
    val filters = ids.map(id => new DimensionFilter()
      .setDimensionName("ga:eventLabel")
      .setOperator("EXACT")
      .setExpressions(List(id).asJava)
    )

    val result = analytics.getReport(ids, Seq("uniqueEvents"), Seq("eventLabel"), filters)
    result.getReports.asScala.map { report =>
      Option(report.getData.getRows) match {
        case Some(rows) => rows.asScala.map(AnalyticsReport.fromGaReport)
        case None => Seq.empty[AnalyticsReport]
      }
    }.head
  }
}

