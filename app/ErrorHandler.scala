import javax.inject.Inject

import com.codahale.metrics.MetricRegistry.name
import com.kenshoo.play.metrics.{Metrics, MetricsFilter}
import play.api.{Environment, Mode}
import play.api.http.HttpErrorHandler
import play.api.mvc.{RequestHeader, Results}

import scala.concurrent.Future

/**
  * Records metrics when errors are encountered and also deals custom error views.
  *
  * TODO serve JSON response when we requested a JSON API.
  */
class ErrorHandler @Inject()(environment: Environment, metrics: Metrics) extends HttpErrorHandler with Results {

  lazy private val internalServerErrorMeter = metrics.defaultRegistry.meter(name(classOf[MetricsFilter], "500"))

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      statusCode match {
        case 404 => NotFound((views.html.errors.notFound()))
        case _ => Status(statusCode)(views.html.errors.clientError(statusCode, message))
      }
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    internalServerErrorMeter.mark()
    Future.successful(
      InternalServerError(views.html.errors.serverError(exception, environment.mode))
    )
  }

}
