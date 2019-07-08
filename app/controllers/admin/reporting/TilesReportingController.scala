package controllers.admin.reporting

import controllers.MyController
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import services.SecurityService
import services.reporting.TilesReportingService
import system.Roles

@Singleton
class TilesReportingController @Inject()(
  tilesReportingService: TilesReportingService,
  securityService: SecurityService,
) extends MyController with I18nSupport {

  import Roles._
  import securityService._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val averageTileAreas = tilesReportingService.getAllUserTileAreas()

    val averageTileHeights = tilesReportingService.getAllUserTileHeights()

    val tileRankingsByArea = tilesReportingService.getAllUserTileAreas().mapValues {
      case x if x == 0 => 0
      case x if x <= 4 => 6 - x
      case x if x >= 5 => 1
    }

    val tileRankings = tilesReportingService.getAllUserTileHeights().map {
      case (tile, ranking) => {
        (tile,
        if(tileRankingsByArea(tile) == 0)  0
        else if (ranking <= 2) tileRankingsByArea(tile)
        else if (ranking <= 4) 1 + tileRankingsByArea(tile)
        else if (ranking <= 6) 2 + tileRankingsByArea(tile)
        else if (ranking <= 8) 3 + tileRankingsByArea(tile)
        else if (ranking <= 10) 4 + tileRankingsByArea(tile)
        else 5 + tileRankingsByArea(tile)
        )
      }
    }

    Ok(views.html.admin.reporting.populartiles.index(averageTileAreas, averageTileHeights, tileRankings))
  }

}
