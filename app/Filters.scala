import javax.inject.Inject
import com.kenshoo.play.metrics.MetricsFilter
import org.databrary.PlayLogbackAccessFilter
import play.api.http.HttpFilters
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter
import system.WarwickSSOFilter

class Filters @Inject()(
  warwickSSOFilter: WarwickSSOFilter,
  securityHeadersFilter: SecurityHeadersFilter,
  accessLog: PlayLogbackAccessFilter,
  metricsFilter: MetricsFilter,
  gzipFilter: GzipFilter,
  corsFilter: CORSFilter,
  csrfFilter: CSRFFilter
) extends HttpFilters {
  def filters = Seq(warwickSSOFilter, accessLog, csrfFilter, corsFilter, securityHeadersFilter, gzipFilter, metricsFilter)
}
