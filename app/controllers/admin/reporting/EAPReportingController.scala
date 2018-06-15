package controllers.admin.reporting

import controllers.MyController
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.i18n.I18nSupport
import services.SecurityService
import services.reporting.EAPReportingService
import system.Roles

@Singleton
class EAPReportingController @Inject()(
  eapReportingService: EAPReportingService,
  securityService: SecurityService,
) extends MyController with I18nSupport {

  import Roles._
  import securityService._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.reporting.eap.index(
      DateTime.now,
      eapReportingService.getMembershipByType().toSeq.sortBy({ case (_, count) => -count }),
      eapReportingService.getMembershipByDepartment().toSeq.sortBy({ case (_, count) => -count })
    ))
  }
}
