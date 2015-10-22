package controllers

import javax.inject.{Inject, Singleton}

import actors.WebsocketActor.TileUpdate
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._

import actors.WebsocketActor

import play.api.libs.ws._
import warwick.sso.SsoClient

@Singleton
class ApplicationController @Inject()(
                                       ws: WSClient,
                                       ssoClient: SsoClient
                                       ) extends Controller {

  def index = ssoClient.Lenient { request =>
    val name = request.context.user.flatMap(_.name.full).getOrElse("nobody")
        Ok(views.html.index(s"${name}"))
  }

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebsocketActor.props(out)
  }

}
