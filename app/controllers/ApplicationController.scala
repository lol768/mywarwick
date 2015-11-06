package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import actors.WebsocketActor

import warwick.sso.SSOClient

import scala.concurrent.Future

class ApplicationController @Inject() (
  ssoClient: SSOClient
) extends Controller {

  val logger = Logger(getClass)

  def index = ssoClient.Lenient { request =>
    val name = request.context.user.flatMap(_.name.full).getOrElse("nobody")
    implicit val linkGenerator = ssoClient.linkGenerator(request)
        Ok(views.html.index(s"${name}"))
  }

  def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    ssoClient.withUser(request) { loginContext =>
      val who = loginContext.user.map(_.usercode).getOrElse("nobody")
      logger.info(s"Websocket opening for ${who}")
      Future.successful(Right(WebsocketActor.props(loginContext) _))
    }
  }

}
