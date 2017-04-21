package controllers.api

import javax.inject.Singleton

import controllers.BaseController
import play.api.Logger
import play.api.libs.json.{JsNumber, JsString, JsValue}
import play.api.mvc.Action

import scala.util.{Failure, Try}
import scala.util.matching.Regex

@Singleton
class ErrorsController extends BaseController {

  override lazy val logger = Logger("JAVASCRIPT_ERROR")

  def js = Action { implicit request =>
    request.body.asJson.flatMap(_.validate[Seq[Map[String, JsValue]]].asOpt).toSeq.flatten.foreach { error =>
      Try(new JsException(error))
        .recoverWith {
          case parseException =>
            logger.error(parseException.getMessage, parseException)
            Failure(parseException)
        }
        .toOption
        .map { jsException =>
          logger.error(jsException.getMessage, jsException)
          jsException
        }.getOrElse(
          logger.error(error.map { case (k, v) => s"$k=$v" }.mkString(" "))
        )
    }

    Ok("")
  }

}

object JsException {
  val lineRegex: Regex = "^\\s*at\\s(.+)(?:\\.(.+))*\\s\\((.+):(\\d+):\\d+\\)".r
}

class JsException(errorMap: Map[String, JsValue]) extends RuntimeException {

  private val (message, stack, source, line) = {
    val seq = Seq("message", "stack", "source", "line").map(k =>
      errorMap.getOrElse(k, throw new IllegalArgumentException(s"Could not find $k in error map"))
    )
    seq match {
      case Seq(a: JsString, b: JsString, c: JsString, d: JsNumber) =>
        (a.value, b.value, c.value, d.value.intValue())
      case _ =>
        throw new IllegalArgumentException()
    }
  }

  private val stacktrace =
    Seq(new StackTraceElement("", "", source, line)) ++
      stack.split("\n").drop(1).map {
        case JsException.lineRegex(className, method, fileName, lineNumber) =>
          new StackTraceElement(className, Option(method).getOrElse(""), fileName, lineNumber.toInt)
        case stackLine =>
          throw new IllegalArgumentException(s"Could not parse line $stackLine")
      }

  override def getMessage: String = {
    message
  }

  override def getStackTrace: Array[StackTraceElement] = {
    stacktrace.toArray
  }

}
