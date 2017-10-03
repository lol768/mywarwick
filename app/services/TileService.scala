package services

import com.google.inject.{ImplementedBy, Inject}
import models._
import play.api.db.{Database, NamedDatabase}
import services.dao.{TileDao, TileLayoutDao}
import warwick.sso.User

@ImplementedBy(classOf[TileServiceImpl])
trait TileService {

  def getTilesByIds(user: Option[User], ids: Seq[String]): Seq[TileInstance]

  def getTilesForUser(user: Option[User]): Seq[TileInstance]

  def getTileLayoutForUser(user: Option[User]): Seq[TileLayout]

  def saveTilePreferencesForUser(user: User, tileLayout: Seq[UserTileSetting]): Unit

  def saveTileLayoutForUser(user: User, tileLayout: Seq[TileLayout]): Unit

}

class TileServiceImpl @Inject()(
  tileDao: TileDao,
  tileLayoutDao: TileLayoutDao,
  @NamedDatabase("default") db: Database
) extends TileService {

  override def getTileLayoutForUser(user: Option[User]): Seq[TileLayout] = db.withConnection {
    implicit c =>
      val userTileLayout = user.map { u =>
        tileLayoutDao.getTileLayoutForUser(u.usercode.string)
      }.getOrElse(Seq.empty)

      if (userTileLayout.isEmpty) {
        user match {
          case Some(u) => getGroups(u).flatMap(group =>
            tileLayoutDao.getDefaultTileLayoutForGroup(group)
          ).toSeq
          case None => tileLayoutDao.getDefaultTileLayoutForGroup("anonymous")
        }
      }
      else {
        userTileLayout
      }
  }

  override def saveTileLayoutForUser(user: User, tileLayout: Seq[TileLayout]) =
    db.withConnection(implicit c => tileLayoutDao.saveTileLayoutForUser(user.usercode.string, tileLayout))

  override def getTilesByIds(user: Option[User], ids: Seq[String]): Seq[TileInstance] =
    db.withConnection {
      implicit c =>
        user match {
          case Some(u) => tileDao.getTilesByIds(u.usercode.string, ids, getGroups(u))
          case None => tileDao.getTilesForAnonymousUser.filter(instance => ids.contains(instance.tile.id))
        }
    }

  override def getTilesForUser(user: Option[User]): Seq[TileInstance] =
    db.withConnection {
      implicit c =>
        user match {
          case Some(u) => tileDao.getTilesForUser(u.usercode.string, getGroups(u))
          case None => tileDao.getTilesForAnonymousUser
        }
    }

  override def saveTilePreferencesForUser(user: User, tileLayout: Seq[UserTileSetting]): Unit = db.withConnection {
    implicit c =>
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
    val isOther = if (!user.isStaffOrPGR && !user.isStudent) Set("other") else Set()
    isStaff ++ isStudent ++ isOther ++ user.department.flatMap(_.shortName).toSet
  }

}
