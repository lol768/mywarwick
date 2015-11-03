package controllers

import javax.inject.{Inject, Singleton}

import actors.WebsocketActor.TileUpdate
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._

import actors.WebsocketActor

import play.api.libs.ws._
import warwick.sso.SsoClient

import scala.concurrent.Future

class ApplicationController @Inject() (
    ssoClient: SsoClient
  ) extends Controller {

  val logger = Logger(getClass)

  def index = ssoClient.Lenient { request =>
    val name = request.context.user.flatMap(_.name.full).getOrElse("nobody")
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
