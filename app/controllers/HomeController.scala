package controllers

import javax.inject.{Inject, Singleton}

import actors.WebsocketActor
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import services.{PhotoService, SecurityService}
import system.AppMetrics
import warwick.sso._

import scala.concurrent.Future

case class AnalyticsTrackingID(string: String)

@Singleton
class HomeController @Inject()(
  security: SecurityService,
  ssoClient: SSOClient,
  metrics: AppMetrics,
  photoService: PhotoService,
  configuration: Configuration
) extends BaseController {

  import security._

  implicit val analyticsTrackingId: Option[AnalyticsTrackingID] =
    configuration.getString("start.analytics.tracking-id").map(AnalyticsTrackingID.apply)

  def index = Action { request =>
    implicit val links = ssoClient.linkGenerator(request)
    links.setTarget("https://" + request.host + request.path)
    Ok(views.html.index())
  }

  def indexTile(id: String) = index

  def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    SecureWebsocket(request) { loginContext: LoginContext =>
      val who = loginContext.user.map(_.usercode).getOrElse("nobody")
      logger.info(s"Websocket opening for ${who}")
      Future.successful(Right(WebsocketActor.props(loginContext, metrics.websocketTracker()) _))
    }
  }

  def photo = RequiredUserAction { request =>
    request.context.user.get.universityId.map { id =>
      val photoUrl = photoService.photo(id)
      Redirect(photoUrl)
    }.getOrElse(
      NotFound
    )
  }
}
