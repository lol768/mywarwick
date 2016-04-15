import javax.inject.Inject

import org.databrary.PlayLogbackAccessApi
import com.kenshoo.play.metrics.MetricsFilter
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject()(
  securityHeadersFilter: SecurityHeadersFilter,
  accessLog: PlayLogbackAccessApi,
  metricsFilter: MetricsFilter,
  gzipFilter: GzipFilter
) extends HttpFilters {
  def filters = Seq(securityHeadersFilter, accessLog.filter, gzipFilter, metricsFilter)
}
