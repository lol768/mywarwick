package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.UserTile
import play.api.libs.json.{Json, JsObject}
import play.api.libs.ws.{WSAPI, WS}
import system.Threadpools

import scala.concurrent.Future

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTileContent(userTile: UserTile): Future[JsObject]

}

class TileContentServiceImpl @Inject() (web: WSAPI) extends TileContentService {
  import Threadpools.tileData

  override def getTileContent(userTile: UserTile): Future[JsObject] =
    web.url(userTile.tile.fetchUrl).get().map { res =>
      res.json.as[JsObject]
    }

}
