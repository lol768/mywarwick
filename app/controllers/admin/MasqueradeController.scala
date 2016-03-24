package controllers.admin

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import services.SecurityService
import system.Roles.Sysadmin

@Singleton
class MasqueradeController @Inject()(
  securityService: SecurityService
) extends BaseController {

  import securityService._

  def masquerade = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.masquerade())
  }

}

