import javax.inject.Inject

import com.kenshoo.play.metrics.MetricsFilter
import org.databrary.PlayLogbackAccessFilter
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject()(
  securityHeadersFilter: SecurityHeadersFilter,
  accessLog: PlayLogbackAccessFilter,
  metricsFilter: MetricsFilter,
  gzipFilter: GzipFilter
) extends HttpFilters {
  def filters = Seq(securityHeadersFilter, accessLog, gzipFilter, metricsFilter)
}
