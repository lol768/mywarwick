package services

import javax.inject.{Inject, Singleton}

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.inject.ImplementedBy
import play.api.db.{Database, NamedDatabase}
import services.dao.UserPreferencesDao
import warwick.core.Logging
import warwick.sso.Usercode

import scala.util.{Failure, Try}

@ImplementedBy(classOf[SmsNotificationsPrefServiceImpl])
trait SmsNotificationsPrefService {

  def get(usercode: Usercode): Boolean

  def set(usercode: Usercode, wantsSMS: Boolean): Unit

  def getNumber(usercode: Usercode): Option[PhoneNumber]

  def setNumber(usercode: Usercode, phoneNumber: Option[PhoneNumber]): Unit

}

@Singleton
class SmsNotificationsPrefServiceImpl @Inject()(
  dao: UserPreferencesDao,
  @NamedDatabase("default") db: Database
) extends SmsNotificationsPrefService with Logging {

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
}
