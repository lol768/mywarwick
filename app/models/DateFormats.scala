package models

import org.joda.time._
import org.joda.time.format._
import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.util._

/**
  * Arbiter of various date formats.
  */
object DateFormats {

  private val emailStart = DateTimeFormat.forPattern("HH:mm 'on' EEE d")
  private val emailEnd = DateTimeFormat.forPattern(" MMM, YYYY")

  /**
    * Friendly date for emails, e.g. "14:36 on Mon 3rd Feb, 2016".
    */
  def emailDateTime(dateTime: ReadableDateTime): String = {
    def ordinal(day: Int) = day % 10 match {
      case _ if (day >= 10 && day <= 20) => "th"
      case 1 => "st"
      case 2 => "nd"
      case 3 => "rd"
      case _ => "th"
    }
    // Joda has no support for ordinals; the easiest way around is to format
    // either side of it and concat together.
    val instant = dateTime.toInstant
    instant.toString(emailStart) + ordinal(dateTime.getDayOfMonth) + instant.toString(emailEnd)
  }

  /** ISO8601 DateTime with millis, for API dates */
  implicit val isoDateWrites = new JodaWrites(ISODateTimeFormat.dateTime())

  /** ISO8601 DateTime (millis optional), for API dates */
  implicit val isoDateReads: Reads[DateTime] = new Reads[DateTime] {
    import ISODateTimeFormat._
    override def reads(json: JsValue): JsResult[DateTime] =
      Try(dateTime().parseDateTime(json.as[String]))
        .orElse(Try(dateTimeNoMillis().parseDateTime(json.as[String]))) match {
        case Success(v) => JsSuccess(v)
        case Failure(e) => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.date.format", "iso8601"))))
      }
  }

  class JodaWrites(fmt: DateTimeFormatter) extends Writes[ReadableInstant] {
    override def writes(o: ReadableInstant): JsValue = JsString(o.toInstant.toString(fmt))
  }

}
