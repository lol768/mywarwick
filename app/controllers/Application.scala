package controllers

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatterBuilder, DateTimeFormatter}
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import actors.{TileActor, TileWebsocketActor}

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    val socketActor = TileWebsocketActor.props(out)
    socketActor
  }

  def cacheManifest = Action {
    // TODO shouldn't use the current time as it will constantly invalidate the manifest -
    // look into calculating the greatest modified timestamp out of all assets + the app build time
    var timestamp = DateTimeFormat.shortDate().print(new DateTime)
    Ok(views.txt.cachemanifest(timestamp)).withHeaders("Content-Type" -> "text/cache-manifest")
  }

}
