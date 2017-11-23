package services

import com.google.inject.{ImplementedBy, Inject}
import models._
import play.api.db.{Database, NamedDatabase}
import services.dao.{TileDao, TileLayoutDao}
import system.{AuditLogContext, Logging}
import warwick.sso.User

@ImplementedBy(classOf[TileServiceImpl])
trait TileService {

  def getTilesByIds(user: Option[User], ids: Seq[String]): Seq[TileInstance]

  def getTilesForUser(user: Option[User]): Seq[TileInstance]

  def getTileLayoutForUser(user: Option[User]): Seq[TileLayout]

  def saveTilePreferencesForUser(user: User, tileLayout: Seq[UserTileSetting])(implicit context: AuditLogContext): Unit

  def saveTileLayoutForUser(user: User, tileLayout: Seq[TileLayout])(implicit context: AuditLogContext): Unit

}

class TileServiceImpl @Inject()(
  tileDao: TileDao,
  tileLayoutDao: TileLayoutDao,
  @NamedDatabase("default") db: Database
) extends TileService with Logging {

  override def getTileLayoutForUser(user: Option[User]): Seq[TileLayout] = db.withConnection {
    implicit c =>
      val userTileLayout = user
        .map { u => tileLayoutDao.getTileLayoutForUser(u.usercode.string) }
        .getOrElse(Nil)

      val userTiles = userTileLayout.map(_.tile).toSet

      // use the default for any tiles that aren't mentioned in the
      // user's layout. Usually a new tile we're introducing.
      val defaults = {
        val groups = user.map(getGroups).getOrElse(Seq("anonymous")).toSeq
        val defaultLayouts = groups.flatMap(group =>
          tileLayoutDao.getDefaultTileLayoutForGroup(group)
        )
        defaultLayouts.filterNot(tl => userTiles.contains(tl.tile))
      }

      userTileLayout ++ defaults
  }

  override def saveTileLayoutForUser(user: User, tileLayout: Seq[TileLayout])(implicit context: AuditLogContext): Unit = {
    db.withConnection(implicit c => tileLayoutDao.saveTileLayoutForUser(user.usercode.string, tileLayout))
    auditLog('UpdateTileLayout, 'tile_layout -> tileLayout.map(layout =>
      layout.tile -> Map(
        "layoutWidth" -> layout.layoutWidth,
        "x" -> layout.x,
        "y" -> layout.y,
        "width" -> layout.width,
        "height" -> layout.height
      )
    ).toMap)
  }

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

  override def saveTilePreferencesForUser(user: User, tileLayout: Seq[UserTileSetting])(implicit context: AuditLogContext): Unit = db.withConnection {
    implicit c =>
      val defaultTileIds = tileDao.getDefaultTilesForGroups(getGroups(user)).map(_.tile.id)
      val previousTiles = getTilesForUser(Some(user))
      // Tiles not sent in the request
      val missingTileIds = defaultTileIds.toSet -- tileLayout.map(_.id)

      val tiles = tileLayout ++ missingTileIds.map(UserTileSetting.removed)

      tileDao.saveTileConfiguration(user.usercode.string, tiles)

      val newVisibleTileIds = tileLayout.filterNot(_.removed).map(_.id).toSet
      val oldVisibleTileIds = previousTiles.filterNot(_.removed).map(_.tile.id).toSet
      val newlyAddedTileIds = newVisibleTileIds -- oldVisibleTileIds
      val newRemovedTileIds = tiles.filter(_.removed).map(_.id).toSet
      val oldRemovedTileIds = previousTiles.filter(_.removed).map(_.tile.id).toSet
      val newlyRemovedTileIds = newRemovedTileIds -- oldRemovedTileIds
      if (newlyAddedTileIds.nonEmpty || newlyRemovedTileIds.nonEmpty) {
        auditLog('UpdateTiles, 'tile_added -> newlyAddedTileIds, 'tile_removed -> newlyRemovedTileIds)
      }
      auditLog('UpdateTilePreferences, 'tile_preferences -> getTilesForUser(Some(user)).map(tileInstance =>
        tileInstance.tile.id -> tileInstance.preferences
      ).toMap)
  }

  // TODO - add undergrad / postgrad groups - review isStaff (should it include PGRs?)
  private def getGroups(user: User): Set[String] = {
    if (user.userSource.contains("WBSLdap") || user.universityId.isEmpty)
      Set("wbs") // tile group is named wbs but also covers users with no Uni ID
    else {
      val isStaff = if (user.isStaffOrPGR) Set("staff") else Set()
      val isStudent = if (user.isStudent) Set("student") else Set()
      val isOther = if (!user.isStaffOrPGR && !user.isStudent) Set("other") else Set()
      isStaff ++ isStudent ++ isOther ++ user.department.flatMap(_.shortName).toSet
    }
  }
}
