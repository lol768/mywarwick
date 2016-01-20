package controllers

import javax.inject.{Inject, Singleton}

import actors.WebsocketActor
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import services.SecurityService
import system.AppMetrics
import uk.ac.warwick.sso.client.SSOClientHandlerImpl._
import uk.ac.warwick.sso.client.{SSOConfiguration}
import warwick.sso._

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
  security: SecurityService,
  ssoClient: SSOClient,
  metrics: AppMetrics,
  ssoConfig: SSOConfiguration
) extends BaseController {

  import security._

  def index = Action { request =>
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

  val SERVICE_SPECIFIC_COOKIE_NAME = ssoConfig.getString("shire.sscookie.name")

  def ssotest = Action { request =>
    val hasGlobalCookie = request.cookies.get(GLOBAL_LOGIN_COOKIE_NAME).isDefined
    val hasServiceCookie = request.cookies.get(SERVICE_SPECIFIC_COOKIE_NAME).isDefined

    Ok(Json.toJson(hasGlobalCookie && !hasServiceCookie))
  }

}
