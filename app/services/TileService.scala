package services

import com.google.inject.{ImplementedBy, Inject}
import models.{TileLayout, TileLayout$, UserTile}
import play.api.db.{Database, NamedDatabase}
import play.api.libs.json._
import services.dao.TileDao
import warwick.sso.User

@ImplementedBy(classOf[TileServiceImpl])
trait TileService {

  def getTilesForUser(user: Option[User]): TileLayout

}

class TileServiceImpl @Inject()(
  tileDao: TileDao,
  @NamedDatabase("default") db: Database
) extends TileService {

  override def getTilesForUser(user: Option[User]): TileLayout =
    db.withConnection { implicit c =>
      user match {
        case Some(u) => TileLayout(tileDao.getTilesForUser(u.usercode.string))
        case None => TileLayout(tileDao.getDefaultTilesConfig)
      }
    }

}