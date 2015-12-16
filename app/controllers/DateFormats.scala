package controllers

import org.joda.time.{ReadableInstant, DateTime}
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.util.Try

object DateFormats {

  implicit val isoDateWrites = new JodaWrites(ISODateTimeFormat.dateTime())

  implicit val isoDateReads: Reads[DateTime] = new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] =
      Try(ISODateTimeFormat.dateTime().parseDateTime(json.as[String]))
        .orElse(Try(ISODateTimeFormat.dateTimeNoMillis().parseDateTime(json.as[String])))
        .map(JsSuccess(_))
        .getOrElse(JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.date.format", "iso8601")))))
  }

  class JodaWrites(fmt: DateTimeFormatter) extends Writes[ReadableInstant] {
    override def writes(o: ReadableInstant): JsValue = JsString(o.toInstant.toString(fmt))
  }

}
