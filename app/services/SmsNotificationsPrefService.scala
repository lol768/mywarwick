package services

import javax.inject.{Inject, Singleton}

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.inject.ImplementedBy
import org.apache.commons.lang3.RandomStringUtils
import play.api.Configuration
import play.api.db.{Database, NamedDatabase}
import play.api.libs.json.JsBoolean
import services.dao.UserPreferencesDao
import services.messaging.SmsSendingService
import warwick.core.Logging
import warwick.sso.Usercode

import scala.util.{Failure, Try}

object SmsNotificationsPrefService {
  private val VERIFICATION_CHARS = "0123456789"
  private val VERIFICATION_CODE_LENGTH = 6
  def generateVerificationCode: String =
    RandomStringUtils.random(VERIFICATION_CODE_LENGTH, VERIFICATION_CHARS).toUpperCase
}

case class SmsVerification(code: String, phoneNumber: PhoneNumber)

@ImplementedBy(classOf[SmsNotificationsPrefServiceImpl])
trait SmsNotificationsPrefService {

  def get(usercode: Usercode): Boolean

  def set(usercode: Usercode, wantsSMS: Boolean): Unit

  def getNumber(usercode: Usercode): Option[PhoneNumber]

  def setNumber(usercode: Usercode, phoneNumber: Option[PhoneNumber]): Unit

  def requireVerification(usercode: Usercode, phoneNumber: PhoneNumber): Option[SmsVerification]

  def getVerification(usercode: Usercode): Option[SmsVerification]

}

@Singleton
class SmsNotificationsPrefServiceImpl @Inject()(
  dao: UserPreferencesDao,
  val configuration: Configuration,
  @NamedDatabase("default") db: Database
) extends SmsNotificationsPrefService with Logging with SmsSendingService {

  override def get(usercode: Usercode): Boolean =
    db.withConnection(implicit c => dao.getUserSmsPreference(usercode))

  override def set(usercode: Usercode, wantsSMS: Boolean): Unit =
    db.withConnection(
      implicit c => dao.setUserSmsPreference(usercode, wantsSMS)
    )

  override def getNumber(usercode: Usercode): Option[PhoneNumber] =
    db.withConnection(implicit c => dao.getUserSmsNumber(usercode).flatMap(numberString =>
      Try(PhoneNumberUtil.getInstance.parse(numberString, "GB")).recoverWith {
        case e =>
          logger.error(s"Unable to parse phone number $numberString", e)
          Failure(e)
      }.toOption
    ))

  override def setNumber(usercode: Usercode, phoneNumber: Option[PhoneNumber]): Unit =
    db.withConnection(implicit c => dao.setUserSmsNumber(
      usercode,
      phoneNumber.map(PhoneNumberUtil.getInstance.format(_, PhoneNumberFormat.INTERNATIONAL)).orNull
    ))

  override def requireVerification(usercode: Usercode, phoneNumber: PhoneNumber): Option[SmsVerification] = {
    val code = SmsNotificationsPrefService.generateVerificationCode
    db.withConnection { implicit c =>
      dao.setUserSmsVerificationCode(usercode, code)
      dao.setUserSmsVerificationNumber(usercode, PhoneNumberUtil.getInstance.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL))
    }

    sendSms(phoneNumber, s"Your My Warwick SMS verification code is $code").map(json => {
      val result = json.value("success").as[JsBoolean].value
      if (!result) {
        logger.error(s"Failed sending SMS message sent to $usercode: ${json.toString}")
        None
      } else {
        Some(SmsVerification(code, phoneNumber))
      }
    }).recover {
      case e =>
        logger.error(s"Unable to send SMS verification to $phoneNumber", e)
        None
    }.get
  }

  override def getVerification(usercode: Usercode): Option[SmsVerification] = {
    val (codeOption, numberOption) = db.withConnection(implicit c =>
      (dao.getVerificationCode(usercode), dao.getVerificationNumber(usercode))
    )
    codeOption.flatMap(code => numberOption.flatMap(number =>
      Try(PhoneNumberUtil.getInstance.parse(number, "GB")).recoverWith {
        case e =>
          logger.error(s"Unable to parse phone number $number", e)
          Failure(e)
      }.toOption.map(phoneNumber =>
        SmsVerification(code, phoneNumber)
      )
    ))
  }
}
