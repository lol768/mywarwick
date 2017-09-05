package controllers.api

import javax.inject.Singleton

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.inject.Inject
import controllers.BaseController
import models.API
import play.api.data.validation.ValidationError
import play.api.libs.json._
import services.{SecurityService, SmsNotificationsPrefService}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.filter

import scala.util.Try

case class SmsNotificationsRequest(
  wantsSms: Boolean,
  smsNumber: Option[PhoneNumber]
)

object SmsNotificationsRequest {
  implicit val reads: Reads[SmsNotificationsRequest] =
    (
      (__ \ "wantsSms").read[Boolean] and
        (__ \ "smsNumber").readNullable[String](
          filter(ValidationError("Invalid phone number")) { s =>
            s.isEmpty || Try(PhoneNumberUtil.getInstance.parse(s, "GB")).isSuccess
          }).map(o => o.flatMap(s =>
            Option(s).filter(_.nonEmpty).map(PhoneNumberUtil.getInstance.parse(_, "GB"))
        ))
    )(SmsNotificationsRequest.apply _)
}

@Singleton
class SmsNotificationsPrefController @Inject()(
  security: SecurityService,
  notificationPrefService: SmsNotificationsPrefService
) extends BaseController {

  import security._

  def get = RequiredUserAction { request =>
    val user = request.context.user.get

    val data = JsObject(Map(
      "wantsSms" -> JsBoolean(notificationPrefService.get(user.usercode)),
      "smsNumber" -> JsString(notificationPrefService.getNumber(user.usercode).map(
        PhoneNumberUtil.getInstance.format(_, PhoneNumberFormat.INTERNATIONAL)
      ).orNull)
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  def update = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      request.body.asJson.map { body =>
        body.validate[SmsNotificationsRequest] match {
          case JsSuccess(data, _) =>
            if (data.wantsSms && data.smsNumber.isEmpty) {
              BadRequest(Json.toJson(API.Failure[JsObject]("error", Seq(API.Error("invalid-body", "Phone number must be set")))))
            } else {
              notificationPrefService.set(user.usercode, data.wantsSms)
              notificationPrefService.setNumber(user.usercode, data.smsNumber)
              auditLog('SetWantsSms, 'wantsSms -> data.wantsSms)
              auditLog('SetSmsNumber, 'smsNumber -> data.smsNumber.map(_.getRawInput).orNull)
              Ok(Json.toJson(API.Success("ok", "saved")))
            }
          case error: JsError =>
            BadRequest(Json.toJson(API.Failure[JsObject]("error", API.Error.fromJsError(error))))
        }
      }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted SmsNotificationsRequest"))))))
    }.get
  }
}