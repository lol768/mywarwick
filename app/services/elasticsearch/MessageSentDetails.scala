package services.elasticsearch

import play.api.libs.json._
import warwick.sso.Usercode

case class SentDetails(
  sms: Seq[Usercode],
  email: Seq[Usercode],
  mobile: Seq[Usercode]
) {
  // count of usercodes that appear in at-least-one output (sms, email, mobile)
  def distinctCount: Int = (sms ++ email ++ mobile).distinct.size
}

object SentDetails {
  implicit val readsUserCode: Reads[Usercode] = new Reads[Usercode] {
    override def reads(jsVal: JsValue): JsResult[Usercode] = jsVal.validate[String].map(Usercode)
  }
  implicit val writesUsercode: Writes[Usercode] = (o: Usercode) => JsString(o.string)
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