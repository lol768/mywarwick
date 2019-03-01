package models.messaging

import java.time.format.DateTimeFormatter
import java.time.{DateTimeException, LocalTime}
import java.time.temporal.ChronoUnit

import anorm._
import anorm.RowParser
import anorm.SqlParser._
import play.api.libs.json._

object DoNotDisturbPeriod {
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  val rowParser: RowParser[DoNotDisturbPeriod] =
    get[LocalTime]("start_time") ~
    get[LocalTime]("end_time") map {
      case start ~ end =>
        DoNotDisturbPeriod(start, end)
    }

  implicit def columnToLocalTime: Column[LocalTime] =
    Column.nonNull { (value, meta) =>
      val MetaDataItem(qualified, _, _) = meta
      value match {
        case str: String if str.nonEmpty => Right(LocalTime.parse(str))
        case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to DateTime for column $qualified"))
      }
    }
  
  implicit val formats: Format[DoNotDisturbPeriod] = new Format[DoNotDisturbPeriod] {
    override def writes(o: DoNotDisturbPeriod): JsValue = Json.obj(
      "start" -> Json.obj(
        "hr" -> o.start.getHour,
        "min" -> o.start.getMinute
      ),
      "end" -> Json.obj(
        "hr" -> o.end.getHour,
        "min" -> o.end.getMinute
      ),
    )

    override def reads(json: JsValue): JsResult[DoNotDisturbPeriod] =
      try {
        JsSuccess(DoNotDisturbPeriod(
          LocalTime.of(
            (json \ "start" \ "hr").as[Int],
            (json \ "start" \ "min").as[Int]
          ),
          LocalTime.of(
            (json \ "end" \ "hr").as[Int],
            (json \ "end" \ "min").as[Int]
          )
        ))
      } catch {
        case jse: JsResultException => JsError(jse.errors)
        case dte: DateTimeException => JsError(dte.getMessage)
      }
  }
}

case class DoNotDisturbPeriod(start: LocalTime, end: LocalTime) {
  def spansDays: Boolean = start.until(end, ChronoUnit.MINUTES) < 0
}

