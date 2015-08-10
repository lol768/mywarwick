package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import actors.WebsocketActor

import play.api.libs.ws._

@Singleton
class ApplicationController @Inject() (
                                        ws: WSClient
                                        ) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebsocketActor.props(out)
  }

}
