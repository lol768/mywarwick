package controllers.api

import javax.inject.Singleton

import controllers.MyController
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.JsValue

@Singleton
class ErrorsController extends MyController {

  lazy val slf4jLogger: Logger = LoggerFactory.getLogger("JAVASCRIPT_ERROR")

  def js = Action { implicit request =>
    request.body.asJson.flatMap(_.validate[Seq[Map[String, JsValue]]].asOpt).toSeq.flatten.foreach { error =>
      val message = error.get("message").flatMap(_.asOpt[String]).getOrElse("-")
      val stacktrace = StructuredArguments.keyValue("stack_trace", error.get("stack").flatMap(_.asOpt[String]).getOrElse("-"))
      slf4jLogger.info(message, stacktrace)
    }
    Ok("")
  }
}

