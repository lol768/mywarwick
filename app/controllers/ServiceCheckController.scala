package controllers

import play.api.mvc._

class ServiceCheckController extends Controller {

  def gtg = Action {
    Ok("\"OK\"")
  }

}
