package controllers

import javax.inject.{Inject, Singleton}

import actors.WebsocketActor
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import services.SecurityService
import system.AppMetrics
import warwick.sso._

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
  security: SecurityService,
  ssoClient: SSOClient,
  metrics: AppMetrics
) extends BaseController {

  import security._

  def index = Action { request =>
    implicit val links = ssoClient.linkGenerator(request)
    links.setTarget("https://" + request.host + request.path)
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
