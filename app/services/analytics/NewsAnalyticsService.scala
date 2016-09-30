package services.analytics

import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.{AnyContent, Request}

@ImplementedBy(classOf[NewsAnalyticsServiceImpl])
trait NewsAnalyticsService {
  def getClicks(ids: Seq[String]): Seq[CombinedRow]
}

class NewsAnalyticsServiceImpl @Inject()(
  analyticsReportService: AnalyticsReportService
) extends NewsAnalyticsService {

  override def getClicks(ids: Seq[String]) = {
    val PAGE_PATH = "pagePath"
    val paths = ids.map(id => controllers.api.routes.ReadNewsController.redirect(id).url)
    val filters = analyticsReportService.idEqualityFilter(paths, PAGE_PATH)

    analyticsReportService.getReport(paths, Seq("pageviews"), Seq(PAGE_PATH), filters)
  }
}

