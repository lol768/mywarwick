package services

import com.google.inject.{ImplementedBy, Inject}
import models.{EnumUtils, UserTile}
import play.api.libs.json._
import services.dao.TileDao
import warwick.sso.User

@ImplementedBy(classOf[TileServiceImpl])
trait TileService {

  def getTilesData(tiles: Seq[UserTile]): JsValue

  def getTilesConfig(user: Option[User]): Seq[UserTile]
}

class TileServiceImpl @Inject()(
  tileDao: TileDao
) extends TileService {

  override def getTilesData(tiles: Seq[UserTile]): JsValue = {
    // TODO: use url and options from each UserTile to request data
    JsObject(Seq())
  }

  override def getTilesConfig(user: Option[User]): Seq[UserTile] =
    user match {
      case Some(u) => tileDao.getTilesForUser(u.usercode.string)
      case None => tileDao.getDefaultTilesConfig
    }
}