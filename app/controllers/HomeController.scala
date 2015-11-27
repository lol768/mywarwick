package controllers

import javax.inject.Inject

import actors.WebsocketActor
import play.api.Logger
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import services.SecurityService
import warwick.sso._

import scala.concurrent.Future

class HomeController @Inject()(
  security: SecurityService,
  ssoClient: SSOClient
) extends Controller {

  import security._

  val logger = Logger(getClass)

  def index = Action { request =>
    implicit val links = ssoClient.linkGenerator(request)
    Ok(views.html.index())
  }


  def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    SecureWebsocket(request) { loginContext: LoginContext =>
      val who = loginContext.user.map(_.usercode).getOrElse("nobody")
      logger.info(s"Websocket opening for ${who}")
      Future.successful(Right(WebsocketActor.props(loginContext) _))
    }
  }

}
