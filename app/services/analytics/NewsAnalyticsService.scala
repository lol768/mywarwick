package services.analytics

import com.google.api.services.analyticsreporting.v4.model._
import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.{AnyContent, Request}

case class NewsAnalyticsReport(id: String, clicks: String)

object NewsAnalyticsReport {
  def fromGaReport(row: ReportRow): NewsAnalyticsReport =
    NewsAnalyticsReport(
      row.getDimensions.get(0).split("/")(2), // get newsId substring from pagePath
      row.getMetrics.get(0).getValues.get(0)
    )
}

@ImplementedBy(classOf[NewsAnalyticsServiceImpl])
trait NewsAnalyticsService {
  def getClicks(ids: Seq[String])(implicit request: Request[AnyContent]): Seq[NewsAnalyticsReport]
}

class NewsAnalyticsServiceImpl @Inject()(
  analytics: AnalyticsReportService
) extends NewsAnalyticsService {

  override def getClicks(ids: Seq[String])(implicit request: Request[AnyContent]) = {
    val PAGE_PATH = "pagePath"
    val paths = ids.map(id => controllers.api.routes.ReadNewsController.redirect(id).url)
    val filters = analytics.idEqualityFilter(paths, PAGE_PATH)

    val results = analytics.getReport(paths, Seq("pageviews"), Seq(PAGE_PATH), filters)
    results.map(NewsAnalyticsReport.fromGaReport)
  }
}

