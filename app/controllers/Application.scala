package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import actors.{TileActor, TileWebsocketActor}

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Hello"))
  }

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    val socketActor = TileWebsocketActor.props(out)
    socketActor
  }

}
