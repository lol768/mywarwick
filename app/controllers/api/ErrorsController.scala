package controllers.api

import controllers.BaseController
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.Action

class ErrorsController extends BaseController {

  override lazy val logger = Logger("JAVASCRIPT_ERROR")

  def js = Action { implicit request =>
    request.body.asJson.flatMap(_.validate[Seq[Map[String, JsValue]]].asOpt).toSeq.flatten.foreach { error =>
      val values = error.map { case (k, v) => s"$k=$v" }.mkString(" ")

      logger.error(values)
    }
    Ok("")
  }

}
