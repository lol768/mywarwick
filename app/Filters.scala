import javax.inject.Inject

import org.databrary.PlayLogbackAccessApi
import com.kenshoo.play.metrics.MetricsFilter
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter

class Filters @Inject()(
  accessLog: PlayLogbackAccessApi,
  metricsFilter: MetricsFilter,
  gzipFilter: GzipFilter
) extends HttpFilters {
  def filters = Seq(accessLog.filter, gzipFilter, metricsFilter)
}
