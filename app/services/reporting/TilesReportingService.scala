package services.reporting

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.{Tile, UserTileLayout}
import play.api.db.{Database, NamedDatabase}
import services.dao.{TileDao, TileLayoutDao}

@ImplementedBy(classOf[TilesReportingServiceImpl])
trait TilesReportingService {

  def getAllUserTileAreas(): Map[Tile, Int]

  def getAllUserTileHeights(): Map[Tile, Int]

  def getAllUserTileLayoutSettings(): Map[Tile, Seq[UserTileLayout]]

}


@Singleton
class TilesReportingServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  tileDao: TileDao,
  tileLayoutDao: TileLayoutDao,
) extends TilesReportingService {


  override def getAllUserTileLayoutSettings() = {
    db.withConnection(implicit c => {
      val allTiles = tileDao.getAllTiles()
      val allUserTiles = tileLayoutDao.getAllUserTileLayouts()
      allTiles.map(tile => (tile, allUserTiles.filter(_.tileId == tile.id))).toMap
    })
  }

  override def getAllUserTileAreas() = {
    db.withConnection(implicit c => {
      this.getAllUserTileLayoutSettings.map {
        case (tile, userTileLayouts) =>
          (tile, if(userTileLayouts.map(layout => layout.width * layout.height).sum == 0) 0
            else userTileLayouts.map(layout => layout.width * layout.height).sum / userTileLayouts.length)
      }
    })
  }

  override def getAllUserTileHeights() = {
    db.withConnection(implicit c => {
      this.getAllUserTileLayoutSettings.map {
        case (tile, userTileLayouts) =>
          (tile, if(userTileLayouts.map(layout => layout.y).sum == 0) 0
          else userTileLayouts.map(layout => layout.y).sum / userTileLayouts.length)
      }
    })
  }


}
