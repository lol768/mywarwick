package system

import javax.inject.Inject

import com.codahale.metrics.MetricRegistry.name
import com.kenshoo.play.metrics.{Metrics, MetricsFilter}
import play.api.Environment
import play.api.http.HttpErrorHandler
import play.api.mvc.{RequestHeader, Results}
import services.FeaturesService
import warwick.sso.SSOClient

import scala.concurrent.Future

/**
  * Records metrics when errors are encountered and also deals custom error views.
  *
  * TODO serve JSON response when we requested a JSON API.
  */
class ErrorHandler @Inject()(environment: Environment, metrics: Metrics, sso: SSOClient, csrfHelperFactory: CSRFPageHelperFactory, featuresService: FeaturesService)
  extends HttpErrorHandler with Results with Logging {

  lazy private val internalServerErrorMeter = metrics.defaultRegistry.meter(name(classOf[MetricsFilter], "500"))

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      statusCode match {
        case 404 => NotFound(views.html.errors.notFound()(requestContext(request)))
        case _ => Status(statusCode)(views.html.errors.clientError(statusCode, message)(requestContext(request)))
      }
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    markInternalServerError()
    logger.error(exception.getMessage, exception)
    Future.successful(
      InternalServerError(views.html.errors.serverError(exception, environment.mode)(requestContext(request)))
    )
  }

  def markInternalServerError() = internalServerErrorMeter.mark()

  private def requestContext(request: RequestHeader) =
    RequestContext.authenticated(sso, request, csrfHelperFactory, featuresService)

}
