package services.elasticsearch

import play.api.libs.json._
import warwick.sso.Usercode

case class SentDetails(
  sms: Seq[Usercode],
  email: Seq[Usercode],
  mobile: Seq[Usercode]
)

object SentDetails {
  implicit val readsUserCode: Reads[Usercode] = Json.reads[Usercode]
  implicit val writesUsercode: Writes[Usercode] = new Writes[Usercode] {
    override def writes(o: Usercode): JsValue = JsString(o.string)
  }
  implicit val formatUsercode: Format[Usercode] = Format(readsUserCode, writesUsercode)
  implicit val format: Format[SentDetails] = Json.format[SentDetails]
}

case class MessageSentDetails(
  successful: SentDetails,
  failed: SentDetails,
  skipped: SentDetails
)

object MessageSentDetails {
  implicit val format: Format[MessageSentDetails] = Json.format[MessageSentDetails]
}