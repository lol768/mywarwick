import com.codahale.metrics.MetricRegistry.name
import com.kenshoo.play.metrics.{Metrics, MetricsFilter}
import play.api.Play.current
import play.api.mvc.{Action, Handler, RequestHeader}
import play.api.{GlobalSettings, Play}

object Global extends GlobalSettings {

  override def onRequestReceived(request: RequestHeader): (RequestHeader, Handler) = {
    try {
      super.onRequestReceived(request)
    } catch {
      case e: Exception =>
        markInternalServerError()
        (request, Action.async(onError(request, e)))
    }
  }

  private def markInternalServerError() = {
    val metrics = Play.application.injector.instanceOf[Metrics]
    metrics.defaultRegistry.meter(name(classOf[MetricsFilter], "500")).mark()
  }

}
