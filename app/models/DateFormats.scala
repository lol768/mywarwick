package models

import org.joda.time._
import org.joda.time.format._
import play.api.data.FormError
import play.api.data.Forms._
import play.api.data.format.{Formats, Formatter}
import play.api.data.validation.ValidationError
import play.api.libs.json._
import system.TimeZones

import scala.Either
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
      case _ if day >= 10 && day <= 20 => "th"
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

  object LocalDateTimeFormatter extends Formatter[LocalDateTime] {
    private val date = ISODateTimeFormat.date()
    private val timeParser = ISODateTimeFormat.localTimeParser
    private val hms = ISODateTimeFormat.hourMinuteSecond()
    private val parser = new DateTimeFormatterBuilder()
      .append(date).appendLiteral('T').append(timeParser).toParser
    private val printer = new DateTimeFormatterBuilder()
      .append(date).appendLiteral('T').append(hms).toPrinter
    private val formatter = new DateTimeFormatterBuilder()
      .append(printer, parser).toFormatter

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDateTime] =
      try {
        data.get(key).map(formatter.parseLocalDateTime).map(Right(_)).getOrElse {
          Left(Seq(FormError(key, "missing")))
        }
      } catch {
        case e: IllegalArgumentException => Left(Seq(FormError(key, "badness")))
      }
    override def unbind(key: String, value: LocalDateTime): Map[String, String] = ???
  }

  /**
    * Form mapping for a datetime-local input type, reading into a Joda DateTime.
    * TODO try using the thing above that is more lenient about parsing seconds etc.
    */
  val dateTimeLocalMapping = of(LocalDateTimeFormatter)

  class JodaWrites(fmt: DateTimeFormatter) extends Writes[ReadableInstant] {
    override def writes(o: ReadableInstant): JsValue = JsString(o.toInstant.toString(fmt))
  }

}
