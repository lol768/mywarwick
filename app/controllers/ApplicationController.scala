package controllers

import javax.inject.Singleton

import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import actors.WebsocketActor

@Singleton
class ApplicationController extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebsocketActor.props(out)
  }

}
