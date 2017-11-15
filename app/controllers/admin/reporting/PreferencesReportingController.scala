package controllers.admin.reporting

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models.{ActivityMute, ActivityProvider}
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
      .sortBy(item => item._1.displayName.getOrElse(item._1.id)): _*)
    Ok(views.html.admin.reporting.preferences.index(allMutes))
  }

}
