package controllers.api

import javax.inject.Singleton

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.inject.Inject
import controllers.BaseController
import models.API
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.filter
import play.api.libs.json._
import play.api.mvc.{AnyContent, Result}
import services.{SecurityService, SmsNotificationsPrefService}
import uk.ac.warwick.util.core.StringUtils
import warwick.sso.{AuthenticatedRequest, User}

import scala.util.Try

case class SmsNotificationsRequest(
  wantsSms: Boolean,
  smsNumber: Option[PhoneNumber],
  verificationCode: Option[String],
  resendVerification: Boolean
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
        )) and
        (__ \ "verificationCode").readNullable[String] and
        (__ \ "resendVerification").readNullable[Boolean].map(_.getOrElse(false))
      ) (SmsNotificationsRequest.apply _)
}

@Singleton
class SmsNotificationsPrefController @Inject()(
  security: SecurityService,
  notificationPrefService: SmsNotificationsPrefService
) extends BaseController {

  private val phoneUtil = PhoneNumberUtil.getInstance

  import security._

  def get = RequiredUserAction { request =>
    val user = request.context.user.get

    val data = JsObject(Map(
      "wantsSms" -> JsBoolean(notificationPrefService.get(user.usercode)),
      "smsNumber" -> JsString(notificationPrefService.getNumber(user.usercode).map(
        phoneUtil.format(_, PhoneNumberFormat.INTERNATIONAL)
      ).orNull)
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  private def requireVerification(user: User, data: SmsNotificationsRequest)(implicit request: AuthenticatedRequest[AnyContent]): Result = {
    val verification = notificationPrefService.requireVerification(user.usercode, data.smsNumber.get)
    if (verification.nonEmpty) {
      Ok(Json.toJson(API.Success("verificationRequired", "verificationRequired")))
    } else {
      InternalServerError(Json.toJson(API.Failure[JsObject]("internal server error", Seq(API.Error("sms-failure", "Unable to send SMS verification code")))))
    }
  }

  private def doUpdate(user: User, data: SmsNotificationsRequest)(implicit request: AuthenticatedRequest[AnyContent]): Result = {
    notificationPrefService.set(user.usercode, data.wantsSms)
    notificationPrefService.setNumber(user.usercode, data.smsNumber)
    auditLog('SetWantsSms, 'wantsSms -> data.wantsSms)
    auditLog('SetSmsNumber, 'smsNumber -> data.smsNumber.map(_.getRawInput).orNull)
    Ok(Json.toJson(API.Success("ok", "saved")))
  }

  def update = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      request.body.asJson.map { body =>
        body.validate[SmsNotificationsRequest] match {
          case JsSuccess(data, _) =>
            if (data.wantsSms && data.smsNumber.isEmpty) {
              BadRequest(Json.toJson(API.Failure[JsObject]("error", Seq(API.Error("invalid-body", "Phone number must be set")))))
            } else {
              val existingNumber = notificationPrefService.getNumber(user.usercode)
              val existingVerification = notificationPrefService.getVerification(user.usercode)

              if (data.resendVerification) {
                requireVerification(user, data)
              } else if (data.smsNumber.nonEmpty && existingVerification.nonEmpty) {
                // Check verification
                if (data.verificationCode.isEmpty || !StringUtils.hasText(data.verificationCode.get)) {
                  // Check code exists
                  BadRequest(Json.toJson(API.Failure[JsObject]("verificationRequired", Seq(API.Error("invalid-body-verification", "Verification code required")))))
                } else if (existingVerification.get.code != data.verificationCode.get) {
                  // Check code matches
                  BadRequest(Json.toJson(API.Failure[JsObject]("verificationRequired", Seq(API.Error("invalid-body-verification", "Verification code is incorrect")))))
                } else if (!existingVerification.get.phoneNumber.exactlySameAs(data.smsNumber.get)) {
                  // Check number matches
                  BadRequest(Json.toJson(API.Failure[JsObject]("verificationRequired", Seq(API.Error("invalid-body-verification", "Verification code sent for different phone number. Click re-send to regenerate code.")))))
                } else {
                  doUpdate(user, data)
                }
              } else if (data.smsNumber.nonEmpty && (existingNumber.isEmpty || !existingNumber.get.exactlySameAs(data.smsNumber.get))) {
                if (data.smsNumber.exists(phoneUtil.isValidNumber)) {
                  requireVerification(user, data)
                } else {
                  // Phone number is invalid
                  BadRequest(Json.toJson(API.Failure[JsObject]("error", Seq(API.Error("invalid-body-number", "This isn't a valid phone number")))))
                }
              } else {
                // Phone number hasn't changed or is empty
                doUpdate(user, data)
              }
            }
          case error: JsError =>
            BadRequest(Json.toJson(API.Failure[JsObject]("error", API.Error.fromJsError(error))))
        }
      }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted SmsNotificationsRequest"))))))
    }.get
  }
}
