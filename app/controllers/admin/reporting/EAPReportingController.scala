package controllers.admin.reporting

import controllers.MyController
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent}
import services.SecurityService
import services.reporting.EAPReportingService
import system.Roles
import utils.UserLookupUtils.DepartmentStringer

@Singleton
class EAPReportingController @Inject()(
  eapReportingService: EAPReportingService,
  securityService: SecurityService,
) extends MyController with I18nSupport {

  import Roles._
  import securityService._
  
  private def sum(m: Iterable[(Any, Int)]): Int = m.map(_._2).sum

  def index: Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val typedMembership = eapReportingService
      .getMembershipByType()
      .toSeq
      .sortBy({ case (_, count) => -count })
    
    val deptMembership = eapReportingService
      .getMembershipByDepartment()
      .toSeq
      .sortBy({ case (_, count) => -count })
      .map({case (dept, count) => (dept.toSafeString, count)})
    
    Ok(views.html.admin.reporting.eap.index(
      DateTime.now,
      sum(typedMembership),
      typedMembership,
      deptMembership
    ))
  }
}
