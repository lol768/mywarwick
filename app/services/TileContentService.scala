package services

import com.google.inject.ImplementedBy
import models.UserTile
import play.api.libs.json.{Json, JsObject}

import scala.concurrent.Future

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTileContent(userTile: UserTile): Future[JsObject]

}

class TileContentServiceImpl extends TileContentService {

  override def getTileContent(userTile: UserTile): Future[JsObject] =
    Future.successful(Json.obj())

}
