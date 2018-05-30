package services.reporting

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.{ActivityMute, ActivityProvider, Tile, UserTile}
import play.api.db.{Database, NamedDatabase}
import services.ProviderRender
import services.dao.{ActivityMuteDao, PublisherDao, TileDao, UserPreferencesDao}
import warwick.sso.Usercode

@ImplementedBy(classOf[PreferencesReportingServiceImpl])
trait PreferencesReportingService {

  def userWantsEmails(usercode: Usercode): Boolean

  def userWantsSMS(usercode: Usercode): Boolean

  def getActiveMutesByProviders(providers: Seq[ActivityProvider]): Map[ActivityProvider, Seq[ActivityMute]]

  def getMutesCountByProviders(providers: Seq[ActivityProvider]): Int

  def getAllActiveMutesGroupedByProviders: Map[ActivityProvider, Seq[ActivityMute]]

  def getAllUserTileSettings(): Map[Tile, Seq[UserTile]]

  def getAllUserTileHiddenCounts(): Map[Tile, Int]
}


@Singleton
class PreferencesReportingServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  userPreferencesDao: UserPreferencesDao,
  publisherDao: PublisherDao,
  activityMuteDao: ActivityMuteDao,
  tileDao: TileDao
) extends PreferencesReportingService {
  override def userWantsEmails(usercode: Usercode) = ???

  override def userWantsSMS(usercode: Usercode) = ???

  override def getActiveMutesByProviders(providers: Seq[ActivityProvider]): Map[ActivityProvider, Seq[ActivityMute]] = {
    db.withConnection(implicit c => {
      providers.map(provider => provider ->
        activityMuteDao.mutesForProvider(provider).filter(m => m.expiresAt.isEmpty || m.expiresAt.exists(_.isAfterNow))
      ).toMap
    })
  }

  override def getMutesCountByProviders(providers: Seq[ActivityProvider]) = {
    db.withConnection(implicit c => {
      activityMuteDao.mutesCountForProviders(providers)
    })
  }

  override def getAllActiveMutesGroupedByProviders = {
    db.withConnection(implicit c => {
      this.getActiveMutesByProviders(publisherDao.getAllProviders().map(ProviderRender.toActivityProvider))
    })
  }

  override def getAllUserTileSettings() = {
    db.withConnection(implicit c => {
      val allTiles = tileDao.getAllTiles()
      val allUserTiles = tileDao.getAllUserTiles()
      allTiles.map(tile => (tile, allUserTiles.filter(_.tileId == tile.id))).toMap
    })
  }

  override def getAllUserTileHiddenCounts() = {
    db.withConnection(implicit c => {
      this.getAllUserTileSettings.seq.map {
        case (tile, userTiles) => (tile, userTiles.count(_.removed))
      }
    })
  }
}
