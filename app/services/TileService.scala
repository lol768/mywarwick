package services

import com.google.inject.{ImplementedBy, Inject}
import models._
import play.api.db.{Database, NamedDatabase}
import services.dao.{TileDao, UserTileLayoutDao}
import warwick.sso.User

@ImplementedBy(classOf[TileServiceImpl])
trait TileService {

  def getTilesByIds(user: User, ids: Seq[String]): Seq[TileInstance]

  def getTilesForUser(user: Option[User]): Seq[TileInstance]

  def getTileLayoutForUser(user: Option[User]): Seq[UserTileLayout]

  def saveTilePreferencesForUser(user: User, tileLayout: Seq[UserTileSetting]): Unit

  def saveTileLayoutForUser(user: User, tileLayout: Seq[UserTileLayout]): Unit

}

class TileServiceImpl @Inject()(
  tileDao: TileDao,
  userTileLayoutDao: UserTileLayoutDao,
  @NamedDatabase("default") db: Database
) extends TileService {

  override def getTileLayoutForUser(user: Option[User]): Seq[UserTileLayout] =
    user.map { u =>
      db.withConnection(implicit c => userTileLayoutDao.getTileLayoutForUser(u.usercode.string))
    }.getOrElse(Seq.empty)

  override def saveTileLayoutForUser(user: User, tileLayout: Seq[UserTileLayout]) =
    db.withConnection(implicit c => userTileLayoutDao.saveTileLayoutForUser(user.usercode.string, tileLayout))

  override def getTilesByIds(user: User, ids: Seq[String]): Seq[TileInstance] =
    db.withConnection(implicit c => tileDao.getTilesByIds(user.usercode.string, ids, getGroups(user)))

  override def getTilesForUser(user: Option[User]): Seq[TileInstance] =
    db.withConnection { implicit c =>
      user match {
        case Some(u) => tileDao.getTilesForUser(u.usercode.string, getGroups(u))
        case None => tileDao.getTilesForAnonymousUser
      }
    }

  override def saveTilePreferencesForUser(user: User, tileLayout: Seq[UserTileSetting]): Unit = db.withConnection { implicit c =>
    val defaultTiles = tileDao.getDefaultTilesForGroups(getGroups(user)).map(_.tile.id)
    val currentTiles = tileLayout.map(_.id)
    val removedTiles = defaultTiles.toSet -- currentTiles

    val tiles = tileLayout ++ removedTiles.map(UserTileSetting.removed)

    tileDao.saveTileConfiguration(user.usercode.string, tiles)
  }

  // TODO - add undergrad / postgrad groups - review isStaff (should it include PGRs?)
  private def getGroups(user: User): Set[String] = {
    val isStaff = if (user.isStaffOrPGR) Set("staff") else Set()
    val isStudent = if (user.isStudent) Set("student") else Set()
    isStaff ++ isStudent ++ user.department.flatMap(_.shortName).toSet
  }

}
