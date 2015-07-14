package actors

import akka.actor._
import play.api.libs.json._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.Random

object WebsocketActor  {
  def props(out: ActorRef) = Props(new WebsocketActor(out))
}

/**
 * Websocket-facing actor, wired in to the controller. It receives
 * any messages send from the client in the form of a JsValue. Other
 * actors can also send any kind of message to it.
 *
 * Currently this contains a lot of stuff but only because it's generating
 * a bunch of fake data as we have no backend yet. When it's done, it
 * ought to be pretty slim as it will mainly just subscribe to some actor
 * within the larger system, passing data to the websocket.
 *
 * @param out this output will be attached to the websocket and will send
 *            messages back to the client.
 */
class WebsocketActor(out : ActorRef) extends Actor with ActorLogging {

  // An update to a single tile
  case class TileUpdate(val data: JsValue)

  override def receive = {
    // these will be the TileUpdates we send to ourself.
    case TileUpdate(data) => {
      out ! data
    }
    // we've received some JSON (probably from the web client) -
    // we're indiscriminately responding with some manifest I've invented.
    // It might make more sense to check the contents of `o` first.
    case o : JsValue =>
      out ! Json.obj(
        "type" -> "tiles-description",
        "user" -> Map(
          "userId" -> "cusxyz"
        ),
        "tiles" -> Seq(
          Map(
            "tileId" -> "1",
            "title" -> "Some random value tile",
            "type" -> "numerical"
          ),
          Map(
            "tileId" -> "2",
            "title" -> "Inbox insite",
            "type" -> "activity-stream"
          ),
          Map(
            "tileId" -> "3",
            "title" -> "Office 365",
            "type" -> "activity-stream"
          )
        )
      )
    case nonsense => log.error(s"Ignoring unrecognised message: ${nonsense}")
  }

  // Send some ActivityStream items regularly
  context.system.scheduler.schedule(0 millis, (1400 + Random.nextInt(400)) millis) {
    self ! TileUpdate(Json.obj(
      "type" -> "tile-update",
      "tileId" -> "1",
      "collection" -> Json.obj(
        "@context" -> "http://www.w3.org/ns/activitystreams",
        "@type" -> "Collection",
        "totalItems" -> 1,
        "items" -> Json.arr(
          Json.obj(
            "@type" -> "Post",
            "published" -> "2011-02-10T15:04:55Z",
            "generator" -> "http://example.org/activities-app",
            "provider" -> "http://example.org/activity-stream",
            "displayNameMap" -> Map(
              "en" -> "Martin posted a new video to his album.",
              "ga" -> "Martin phost le fisean nua a albam."
            ),
            "actor" -> Json.obj(
              "@type" -> "Person",
              "@id" -> "urn:example:person:martin",
              "displayName" -> "Martin Smith",
              "url" -> "http://example.org/martin",
              "image" -> Json.obj(
                "@type" -> "Link",
                "href" -> "http://example.org/martin/image",
                "mediaType" -> "image/jpeg",
                "width" -> 250,
                "height" -> 250
              )
            ),
            "object" -> Json.obj(
              "@type" -> "Image",
              "@id" -> "http://example.org/album/my_fluffy_cat",
              "preview" -> Map(
                "@type" -> "Link",
                "href" -> "http://example.org/album/my_fluffy_cat_thumb.jpg",
                "mediaType" -> "image/jpeg"
              ),
              "url" -> Seq(
                Map(
                  "@type" -> "Link",
                  "href" -> "http://example.org/album/my_fluffy_cat.jpg",
                  "mediaType" -> "image/jpeg"
                ),
                Map(
                  "@type" -> "Link",
                  "href" -> "http://example.org/album/my_fluffy_cat.png",
                  "mediaType" -> "image/png"
                )
              )
            ),
            "target" -> Json.obj(
              "@type" -> "Album",
              "@id" -> "http://example.org/album/",
              "displayNameMap" -> Map(
                "en" -> "Martin's Photo Album",
                "ga" -> "Grianghraif Mairtin"
              ),
              "image" -> Map(
                "@type" -> "Link",
                "href" -> "http://example.org/album/thumbnail.jpg",
                "mediaType" -> "image/jpeg"
              )
            )
          )
        )
      )
    ))
  }

  context.system.scheduler.schedule(0 millis, (1400 + Random.nextInt(400)) millis) {
    self ! TileUpdate(Json.obj(
      "type" -> "tile-update",
      "tileId" -> "2",
      "value" -> Random.nextInt(1000)
    ))
  }

  context.system.scheduler.schedule(0 millis, (1400 + Random.nextInt(400)) millis) {
    self ! TileUpdate(Json.obj(
      "type" -> "tile-update",
      "tileId" -> "3",
      "value" -> Random.nextInt(1000)
    ))
  }

}


