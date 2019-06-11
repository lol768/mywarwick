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

    val tileRankingsByArea = tilesReportingService.getAllUserTileAreas().map(tile => {
      (tile._1,
      tile._2 match {
        case x if x == 0 => 0
        case x if x <= 4 => 6 - x
        case x if x >= 5 => 1
      })
    })

    val tileRankings = tilesReportingService.getAllUserTileHeights().map(tile => {
      (tile._1,
        if(tileRankingsByArea(tile._1) == 0) 0
        else if (tile._2 <= 2) tileRankingsByArea(tile._1)
        else if (tile._2 <= 4) 1 + tileRankingsByArea(tile._1)
        else if (tile._2 <= 6) 2 + tileRankingsByArea(tile._1)
        else if (tile._2 <= 8) 3 + tileRankingsByArea(tile._1)
        else if (tile._2 <= 10) 4 + tileRankingsByArea(tile._1)
        else 5 + tileRankingsByArea(tile._1)
      )
    })

    Ok(views.html.admin.reporting.populartiles.index(averageTileAreas, averageTileHeights, tileRankings))
  }

}
