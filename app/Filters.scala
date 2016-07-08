import javax.inject.Inject

import com.kenshoo.play.metrics.MetricsFilter
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
  corsFilter: CORSFilter
) extends HttpFilters {
  def filters = Seq(corsFilter, securityHeadersFilter, accessLog, gzipFilter, metricsFilter)
}
