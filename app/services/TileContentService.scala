package services

import com.google.inject.ImplementedBy
import models.TileInstance$
import play.api.libs.json.{Json, JsObject}

import scala.concurrent.Future

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTileContent(userTile: TileInstance): Future[JsObject]

}

class TileContentServiceImpl extends TileContentService {

  override def getTileContent(userTile: TileInstance): Future[JsObject] =
    Future.successful(Json.obj())

}
