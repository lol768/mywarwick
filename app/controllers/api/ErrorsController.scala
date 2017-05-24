package controllers.api

import collection.JavaConversions._
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
      val errors = mapAsJavaMap(allErrors.filterKeys(key => key == "stack" || key == "message")
        .map { case (key, value) =>
          key match {
            case "stack" =>
              "stack_trace" -> value.toString
            case "message" =>
              "message" -> value.toString
          }
        }).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
      slf4jLogger.info("{}", StructuredArguments.entries(errors));
    }
    Ok("")
  }
}
