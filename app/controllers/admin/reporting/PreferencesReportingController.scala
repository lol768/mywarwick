package controllers.admin.reporting

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import play.api.i18n.{I18nSupport, MessagesApi}
import services.SecurityService
import services.reporting.PreferencesReportingService
import system.Roles

import scala.collection.immutable.ListMap

@Singleton
class PreferencesReportingController @Inject()(
  preferencesReportingService: PreferencesReportingService,
  securityService: SecurityService,
  val messagesApi: MessagesApi
) extends BaseController with I18nSupport {

  import Roles._
  import securityService._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>

    val allMutes = ListMap(preferencesReportingService
      .getAllMutesGroupedByProviders()
      .toSeq
      .sortWith((a, b) => {
        a._1.displayName.getOrElse(a._1.id) < b._1.displayName.getOrElse(b._1.id)
      }): _*)

    Ok(views.html.admin.reporting.preferences.index(allMutes))
  }

}
