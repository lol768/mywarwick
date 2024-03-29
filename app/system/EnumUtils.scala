package system

import play.api.libs.json._

import scala.language.implicitConversions

// Builds JSON reads/writes for Scala enumerations (from http://stackoverflow.com/a/15489179)
object EnumUtils {

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] =
    Format(enumReads(enum), enumWrites)

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
        }
      case _ => JsError("String value expected")
    }
  }

  def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }
}
