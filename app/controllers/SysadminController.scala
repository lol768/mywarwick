package controllers

import com.google.inject.Inject
import services.SecurityService
import system.Roles.Sysadmin

class SysadminController @Inject()(
  securityService: SecurityService
) extends BaseController {

  import securityService._

  def masquerade = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.sysadmin.masquerade())
  }

}

