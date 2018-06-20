package models.messaging

import anorm._
import anorm.RowParser
import anorm.SqlParser._
import play.api.libs.json._

object DoNotDisturbPeriod {
  def is24Hour(hr: Int): Boolean = 0 until 24 contains hr

  def is24Minute(min: Int): Boolean = 0 until 60 contains min

  def validate(doNotDisturbPeriod: DoNotDisturbPeriod): Option[DoNotDisturbPeriod] = {
    import doNotDisturbPeriod._
    if (is24Hour(start.hr) && is24Hour(end.hr) && is24Minute(start.min) && is24Minute(end.min))
      Some(DoNotDisturbPeriod(Time(start.hr, start.min), Time(end.hr, end.min)))
    else
      None
  }

  val rowParser: RowParser[DoNotDisturbPeriod] =
    get[Int]("start_hr") ~
      get[Int]("start_min") ~
      get[Int]("end_hr") ~
      get[Int]("end_min") map {
      case startHr ~ startMin ~ endHr ~ endMin =>
        DoNotDisturbPeriod(Time(startHr, startMin), Time(endHr, endMin))
    }
  
  implicit val formats: Format[DoNotDisturbPeriod] = Json.format[DoNotDisturbPeriod]
}

case class Time(hr: Int, min: Int)
object Time {
  implicit val formats: Format[Time] = Json.format[Time]
}

case class DoNotDisturbPeriod(start: Time, end: Time) {
  def spansDays: Boolean = end.hr < start.hr || (end.hr == start.hr && end.min < start.min)
  def endIsAfter(hr: Int, min: Int): Boolean = end.hr > hr || (end.hr == hr && end.min > min)
  def startIsBeforeOrEqual(hr: Int, min: Int): Boolean = hr > start.hr || (hr == start.hr && min >= start.min)
}

