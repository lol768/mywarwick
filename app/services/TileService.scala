package services

import com.google.inject.{ImplementedBy, Inject}
import models._
import play.api.db.{Database, NamedDatabase}
import services.dao.TileDao
import warwick.sso.User

@ImplementedBy(classOf[TileServiceImpl])
trait TileService {

  def getTilesByIds(user: User, ids: Seq[String]): Seq[TileInstance]

  def getTilesForUser(user: Option[User]): TileLayout

  def saveTileLayoutForUser(user: User, tileLayout: UserTileLayout): Unit

}

class TileServiceImpl @Inject()(
  tileDao: TileDao,
  @NamedDatabase("default") db: Database
) extends TileService {

  override def getTilesByIds(user: User, ids: Seq[String]): Seq[TileInstance] =
    db.withConnection(implicit c => tileDao.getTilesByIds(user.usercode.string, ids, getGroups(user)))

  override def getTilesForUser(user: Option[User]): TileLayout =
    db.withConnection { implicit c =>
      user match {
        case Some(u) => TileLayout(tileDao.getTilesForUser(u.usercode.string, getGroups(u)))
        case None => TileLayout(tileDao.getTilesForAnonymousUser)
      }
    }

  override def saveTileLayoutForUser(user: User, tileLayout: UserTileLayout): Unit = db.withConnection { implicit c =>
    val defaultTiles = tileDao.getDefaultTilesForGroups(getGroups(user)).map(_.tile.id)
    val currentTiles = tileLayout.tiles.map(_.id)
    val removedTiles = defaultTiles.toSet -- currentTiles

    val tiles = tileLayout.tiles ++ removedTiles.map(UserTileSetting.removed)

    tileDao.saveTileLayout(user.usercode.string, UserTileLayout(tiles))
  }

  // TODO - add undergrad / postgrad groups - review isStaff (should it include PGRs?)
  private def getGroups(user: User): Set[String] = {
    val isStaff = if (user.isStaffOrPGR) Set("staff") else Set()
    val isStudent = if (user.isStudent) Set("student") else Set()
    isStaff ++ isStudent ++ user.department.flatMap(_.shortName).toSet
  }

}
