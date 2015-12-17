package controllers

import javax.inject.{Singleton, Inject}

import actors.WebsocketActor
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import services.SecurityService
import system.{AppMetrics, Logging}
import warwick.sso._

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
  security: SecurityService,
  ssoClient: SSOClient,
  metrics: AppMetrics
) extends BaseController {

  import security._

  def index = UserAction { request =>
    implicit val links = ssoClient.linkGenerator(request)
    Ok(views.html.index())
  }

  def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    SecureWebsocket(request) { loginContext: LoginContext =>
      val who = loginContext.user.map(_.usercode).getOrElse("nobody")
      logger.info(s"Websocket opening for ${who}")
      Future.successful(Right(WebsocketActor.props(loginContext, metrics.websocketTracker()) _))
    }
  }

}
