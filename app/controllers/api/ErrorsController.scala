package controllers.api

import collection.JavaConverters._
import javax.inject.Singleton

import controllers.BaseController
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.JsValue
import play.api.mvc.Action

@Singleton
class ErrorsController extends BaseController {

  lazy val slf4jLogger: Logger = LoggerFactory.getLogger("JAVASCRIPT_ERROR")

  def js = Action { implicit request =>
    request.body.asJson.flatMap(_.validate[Seq[Map[String, JsValue]]].asOpt).toSeq.flatten.foreach { allErrors =>
      val message = allErrors.getOrElse("message", "-").toString()
      val stacktrce = StructuredArguments.keyValue("stack_trace", allErrors.getOrElse("stack", "-").toString())
      slf4jLogger.info(message, stacktrce)
    }
    Ok("")
  }
}

