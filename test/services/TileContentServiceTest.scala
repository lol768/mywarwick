package services

import com.google.inject.Guice
import helpers.TestApplications
import models._
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.inject.BuiltinModule
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceInjectorBuilder}
import play.api.libs.json.Json
import play.api.libs.ws.ning.{NingWSAPI, NingWSModule}
import play.core.server._
import play.api.routing.sird._
import play.api.mvc._
import Results._

class TileContentServiceTest extends PlaySpec with ScalaFutures {

  type RouteFunction = PartialFunction[RequestHeader, Handler]

  val config = Configuration.from(Map("play.http" -> Map()))
  val app = TestApplications.minimal()
  val injector = app.injector

  val routes: RouteFunction = {
    case GET(p"/content/printcredits") => Action {
      Ok(API.success(
        "value" -> "£1.33"
      ))
    }
  }

  def userPrinterTile(url: String) = UserTile(
    tile = Tile(
      id = "printcredits",
      defaultSize = TileSize.small,
      fetchUrl = url
    ),
    tileConfig = TileConfig(1, TileSize.small),
    options = None,
    createdAt = new DateTime(),
    updatedAt = new DateTime()
  )

  "TileContentService" should {
    val service = new TileContentServiceImpl(injector.instanceOf[NingWSAPI])

    "fetch a Tile's URL" in {
      runServer(routes) { port =>
        val ut = userPrinterTile(s"http://localhost:${port}/content/printcredits")
        service.getTileContent(ut).futureValue must be (Json.obj())
      }
    }
  }

  def runServer[A](routes: RouteFunction)(block: (Int) => A): A = {
    val port = 19000
    val server = NettyServer.fromRouter(ServerConfig(port = Some(port)))(routes)
    try block(port)
    finally server.stop()
  }

}
