package controllers.admin.reporting

import controllers.MyController
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import services.SecurityService
import services.reporting.PreferencesReportingService
import system.Roles

import scala.collection.immutable.ListMap

@Singleton
class PreferencesReportingController @Inject()(
  preferencesReportingService: PreferencesReportingService,
  securityService: SecurityService,
) extends MyController with I18nSupport {

  import Roles._
  import securityService._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>

    val allActivityMutes = ListMap(preferencesReportingService
      .getAllActiveMutesGroupedByProviders
      .toSeq.sortBy({ case (key, _) => key.displayName.getOrElse(key.id) }): _*)

    val allTileHiddenCount = ListMap(preferencesReportingService
      .getAllUserTileHiddenCounts()
      .toSeq.sortBy({ case(tile, _) => tile.title }): _*)

    Ok(views.html.admin.reporting.preferences.index(
      allActivityMutes,
      allTileHiddenCount
    ))
  }

}
