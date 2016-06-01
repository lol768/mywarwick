package controllers.admin

import javax.inject.Singleton

import controllers.BaseController
import play.api.mvc.Action

@Singleton
class AdminController extends BaseController {

  def index = Action {
    Ok(views.html.admin.index())
  }

}
