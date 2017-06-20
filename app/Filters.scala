import javax.inject.Inject

import com.kenshoo.play.metrics.MetricsFilter
import filters.RouteExemptCsrfFilter
import org.databrary.PlayLogbackAccessFilter
import play.api.http.HttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject()(
  securityHeadersFilter: SecurityHeadersFilter,
  accessLog: PlayLogbackAccessFilter,
  metricsFilter: MetricsFilter,
  gzipFilter: GzipFilter,
  corsFilter: CORSFilter,
  rxCsrfFilter: RouteExemptCsrfFilter
) extends HttpFilters {
  def filters = Seq(rxCsrfFilter, corsFilter, securityHeadersFilter, accessLog, gzipFilter, metricsFilter)
}
