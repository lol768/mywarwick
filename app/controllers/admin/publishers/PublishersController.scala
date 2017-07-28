package controllers.admin.publishers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import services.SecurityService
import system.Roles

@Singleton
class PublishersController @Inject() (
  security: SecurityService
) extends BaseController {

  import Roles._
  import security._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.publishers.index())
  }

}
