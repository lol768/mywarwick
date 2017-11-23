package controllers.admin

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import services.SecurityService
import system.Roles

@Singleton
class AdminController @Inject() (
  security: SecurityService
) extends MyController {

  import Roles._
  import security._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.index())
  }

  def sysadmin = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.sysadmin())
  }

}
