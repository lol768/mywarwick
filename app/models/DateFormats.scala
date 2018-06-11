package models

import org.joda.time._
import org.joda.time.format._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.validation.ValidationError
import play.api.data.{FieldMapping, FormError}
import play.api.libs.json._

import scala.util._

/**
  * Arbiter of various date formats.
  */
object DateFormats {


  //// PLAIN DATE TO STRING PRINTERS

  private val emailStart = DateTimeFormat.forPattern("HH:mm 'on' EEE d")
  private val emailEnd = DateTimeFormat.forPattern(" MMM, YYYY")

  val localDate: DateTimeFormatter = DateTimeFormat.forPattern("EEE d MMM yyyy")

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
    val dt = dateTime.toDateTime
    dt.toString(emailStart) + ordinal(dateTime.getDayOfMonth) + dt.toString(emailEnd)
  }


  ///// JSON FORMATTERS

  /** ISO8601 DateTime with millis, for API dates */
  implicit val isoDateWrites = new JodaWrites(ISODateTimeFormat.dateTime())

  /** ISO8601 DateTime (millis optional), for API dates */
  implicit val isoDateReads: Reads[DateTime] = new Reads[DateTime] {
    import ISODateTimeFormat._
    override def reads(json: JsValue): JsResult[DateTime] =
      Try(dateTime().parseDateTime(json.as[String]))
        .orElse(Try(dateTimeNoMillis().parseDateTime(json.as[String]))) match {
        case Success(v) => JsSuccess(v)
        case Failure(e) => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.date.format", "iso8601"))))
      }
  }

  implicit val isoDateFormats: Format[DateTime] = Format(isoDateReads, isoDateWrites)


  ///// JODA FORMATTERS

  /**
    * For formatting datetime-local input values. Is fairly lenient in parsing,
    * and outputs to second precision.
    */
  object LocalDateTimeFormatter extends Formatter[LocalDateTime] {
    private val parser = new DateTimeFormatterBuilder()
      .append(ISODateTimeFormat.date())
      .appendLiteral('T')
      .append(ISODateTimeFormat.localTimeParser)
      .toParser
    private val printer = new DateTimeFormatterBuilder()
      .append(ISODateTimeFormat.date())
      .appendLiteral('T')
      .append(ISODateTimeFormat.hourMinuteSecond())
      .toPrinter
    private val formatter = new DateTimeFormatterBuilder().append(printer, parser).toFormatter

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDateTime] =
      try {
        data.get(key).map(formatter.parseLocalDateTime).map(Right(_)).getOrElse {
          Left(Seq(FormError(key, "missing")))
        }
      } catch {
        case e: IllegalArgumentException => Left(Seq(FormError(key, "badness")))
      }

    override def unbind(key: String, value: LocalDateTime): Map[String, String] = Map(
      key -> formatter.print(value)
    )
  }

  object LocalDateFormatter extends Formatter[LocalDate] {
    private val parser = new DateTimeFormatterBuilder()
      .append(ISODateTimeFormat.date())
      .toParser
    private val printer = new DateTimeFormatterBuilder()
      .append(ISODateTimeFormat.date())
      .toPrinter
    private val formatter = new DateTimeFormatterBuilder().append(printer, parser).toFormatter

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
      try {
        data.get(key).map(formatter.parseLocalDate).map(Right(_)).getOrElse {
          Left(Seq(FormError(key, "missing")))
        }
      } catch {
        case e: IllegalArgumentException => Left(Seq(FormError(key, "badness")))
      }

    override def unbind(key: String, value: LocalDate): Map[String, String] = Map(
      key -> formatter.print(value)
    )
  }

  class JodaWrites(fmt: DateTimeFormatter) extends Writes[ReadableInstant] {
    override def writes(o: ReadableInstant): JsValue = JsString(o.toInstant.toString(fmt))
  }


  ///// FORM MAPPINGS

  /** Use as a form mapping for a LocalDateTime property against a datetime-local input */
  val dateTimeLocalMapping: FieldMapping[LocalDateTime] = of(LocalDateTimeFormatter)

  val dateLocalMapping: FieldMapping[LocalDate] = of(LocalDateFormatter)

}
