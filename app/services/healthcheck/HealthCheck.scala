package services.healthcheck

import models.DateFormats.isoDateWrites
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import services.healthcheck.HealthCheckStatus.{Critical, Okay, Warn}

import scala.annotation.StaticAnnotation

case class HealthCheckStatus(string: String)

case class PerfData[T](name: String, value: T, warn: Option[T] = None, critical: Option[T] = None, min: Option[T] = None, max: Option[T] = None) {

  def formatted: String = {
    val warnCritical = (for (a <- warn; b <- critical) yield s";$a;$b").getOrElse("")
    val minMax = (for (a <- min; b <- max) yield s";$a;$b").getOrElse("")

    s"$name=$value$warnCritical$minMax"
  }

}

object HealthCheckStatus {
  val Okay = HealthCheckStatus("okay")
  val Warn = HealthCheckStatus("warn")
  val Critical = HealthCheckStatus("critical")
}

abstract class HealthCheck[T](implicit num: Numeric[T]) {

  def name: String

  def status: HealthCheckStatus = {
    val higherIsBetter = num.lt(critical, warn)

    if (higherIsBetter) {
      if (num.gt(value, warn)) Okay
      else if (num.gt(value, critical)) Warn
      else Critical
    } else {
      if (num.lt(value, warn)) Okay
      else if (num.lt(value, critical)) Warn
      else Critical
    }
  }

  def message: String

  def value: T

  def warn: T

  def critical: T

  def perfData: Seq[PerfData[T]] = Seq()

  def testedAt: DateTime

  def toJson: JsObject = Json.obj(
    "name" -> name,
    "status" -> status.string,
    "perfData" -> (Seq(PerfData(name.replace("-", "_"), value, Some(warn), Some(critical)).formatted) ++ perfData.map(_.formatted)),
    "message" -> s"$message (warn: $warn, critical: $critical)",
    "testedAt" -> testedAt
  )

}
