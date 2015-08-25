package controllers

import javax.inject.{Inject, Singleton}

import actors.WebsocketActor.TileUpdate
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._

import actors.{MessageEnvelope, MessageBus, WebsocketActor}

import play.api.libs.ws._

@Singleton
class ApplicationController @Inject() (
                                        messageBus: MessageBus,
                                        ws: WSClient
                                        ) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebsocketActor.props(out, messageBus)
  }

  def showInjectForm = Action {
    Ok(views.html.inject())
  }

  case class InjectData(title: String)

  val injectForm = Form(
    mapping(
      "title" -> text
    )(InjectData.apply)(InjectData.unapply)
  )

  def injectEvent = Action { implicit request =>
    import Json._

    injectForm.bindFromRequest.fold(
      formWithErrors => BadRequest("No mate"),
      data => {
        // Publish a message to the MessageBus, which any listening actor
        // will receive a copy of.
        messageBus.publish(MessageEnvelope(
          topic = "example.topic.testinject",
          payload = TileUpdate(obj(
            "type" -> "tile-update",
            "tileId" -> "3",
            "items" -> arr(
              obj(
                "id" -> "1438336410350",
                "title" -> data.title,
                "links" -> obj("canonical" -> obj( "href" -> "http://www2.warwick.ac.uk/about/warwick50/events/stafffestival" )),
                "published" -> ISODateTimeFormat.dateTime().print(1438336410350L)
              )
            )
          ))
        ))
        Ok("Done")
      }
    )


  }

}
