package controllers.admin

import play.api._
import play.api.mvc._

/**
  * Top level sysadmin controller.
  *
  * We're going to need access to things like SecurityService here,
  * so if we want to keep the separate project then the shared stuff
  * may need splitting into
  */
class AdminController extends Controller {

  // FIXME SecureAction and a sysadmin check here plz
  def index = Action {
    Ok(views.html.admin.index())
  }

}