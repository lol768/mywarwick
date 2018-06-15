package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject, Singleton}
import controllers.api.ColourScheme
import models.FeaturePreferences
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.{Department, User, UserLookupService, Usercode}

import scala.collection.immutable.ListMap

@ImplementedBy(classOf[UserPreferencesDaoImpl])
trait UserPreferencesDao {

  def exists(usercode: Usercode)(implicit c: Connection): Boolean

  def save(usercode: Usercode)(implicit c: Connection): Unit

  def countInitialisedUsers(usercodes: Set[Usercode])(implicit c: Connection): Int

  def allInitialisedUsers()(implicit c: Connection): Seq[Usercode]

  def getNotificationFilter(usercode: Usercode)(implicit c: Connection): JsObject

  def getActivityFilter(usercode: Usercode)(implicit c: Connection): JsObject

  def setNotificationFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit

  def setActivityFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit

  def getUserEmailsPreference(usercode: Usercode)(implicit c: Connection): Boolean

  def setUserEmailsPreference(usercode: Usercode, wantsEmail: Boolean)(implicit c: Connection): Unit

  def getColourSchemePreference(usercode: Usercode)(implicit c: Connection): ColourScheme

  def setColourSchemePreference(usercode: Usercode, chosenScheme: ColourScheme)(implicit c: Connection): Boolean

  def getUserSmsPreference(usercode: Usercode)(implicit c: Connection): Boolean

  def setUserSmsPreference(usercode: Usercode, wantsSms: Boolean)(implicit c: Connection): Unit

  def getUserSmsNumber(usercode: Usercode)(implicit c: Connection): Option[String]

  def setUserSmsNumber(usercode: Usercode, phoneNumber: String)(implicit c: Connection): Unit

  def setUserSmsVerificationCode(usercode: Usercode, code: String)(implicit c: Connection): Boolean

  def getVerificationCode(usercode: Usercode)(implicit c: Connection): Option[String]

  def setUserSmsVerificationNumber(usercode: Usercode, phoneNumber: String)(implicit c: Connection): Boolean

  def getVerificationNumber(usercode: Usercode)(implicit c: Connection): Option[String]

  def getFeaturePreferences(usercode: Usercode)(implicit c: Connection): FeaturePreferences

  def setFeaturePreferences(usercode: Usercode, prefs: FeaturePreferences)(implicit c: Connection): Boolean

  def countEAPByType()(implicit c: Connection): Map[String, Int]

  def countEAPByDepartment()(implicit c: Connection): Map[Option[Department], Int]
}

@Singleton
class UserPreferencesDaoImpl @Inject()(
  userLookupService: UserLookupService
) extends UserPreferencesDao {

  private val featurePreferencesParser =
    get[DateTime]("EAP_UNTIL").?
      .map {
        until => FeaturePreferences(until)
      }

  private val usercodeParser =
    str("USERCODE")
      .map {
        usercode => Usercode(usercode)
      }

  override def exists(usercode: Usercode)(implicit c: Connection): Boolean =
    SQL"SELECT CREATED_AT FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(scalar[DateTime].singleOpt)
      .nonEmpty

  override def save(usercode: Usercode)(implicit c: Connection): Unit =
    SQL"INSERT INTO USER_PREFERENCE (USERCODE, CREATED_AT) VALUES (${usercode.string}, SYSDATE)"
      .execute()

  override def countInitialisedUsers(usercodes: Set[Usercode])(implicit c: Connection): Int =
    usercodes.grouped(1000).map { groupedUsercodes =>
      SQL"SELECT COUNT(*) FROM USER_PREFERENCE WHERE USERCODE IN (${groupedUsercodes.map(_.string)})"
        .as(scalar[Int].single)
    }.sum

  override def allInitialisedUsers()(implicit c: Connection): Seq[Usercode] =
    SQL"SELECT USERCODE FROM USER_PREFERENCE"
      .executeQuery()
      .as(str("usercode").*)
      .map(Usercode)

  override def getNotificationFilter(usercode: Usercode)(implicit c: Connection): JsObject =
    SQL"SELECT NOTIFICATION_FILTER FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(get[Option[String]]("notification_filter").singleOpt.map(
        _.flatten.map(Json.parse(_).as[JsObject]).getOrElse(JsObject(Nil))
      ))

  override def getActivityFilter(usercode: Usercode)(implicit c: Connection): JsObject =
    SQL"SELECT ACTIVITY_FILTER FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(get[Option[String]]("activity_filter").singleOpt.map(
        _.flatten.map(Json.parse(_).as[JsObject]).getOrElse(JsObject(Nil))
      ))

  override def setNotificationFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit =
    SQL"UPDATE USER_PREFERENCE SET NOTIFICATION_FILTER = ${filter.toString} WHERE USERCODE = ${usercode.string}".execute()

  override def setActivityFilter(usercode: Usercode, filter: JsObject)(implicit c: Connection): Unit =
    SQL"UPDATE USER_PREFERENCE SET ACTIVITY_FILTER = ${filter.toString} WHERE USERCODE = ${usercode.string}".execute()

  override def getUserEmailsPreference(usercode: Usercode)(implicit c: Connection): Boolean = {
    val result =
      SQL"SELECT WANTS_EMAILS FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}".as(scalar[Boolean].singleOpt)

    result.getOrElse(true)
  }

  override def setUserEmailsPreference(usercode: Usercode, wantsEmail: Boolean)(implicit c: Connection): Unit = {
    if (!exists(usercode)) {
      save(usercode)
    }

    SQL"""UPDATE USER_PREFERENCE SET WANTS_EMAILS = $wantsEmail WHERE USERCODE = ${usercode.string}""".execute()
  }

  override def getUserSmsPreference(usercode: Usercode)(implicit c: Connection): Boolean = {
    val result =
      SQL"SELECT WANTS_SMS FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}".as(scalar[Boolean].singleOpt)

    result.getOrElse(true)
  }

  override def setUserSmsPreference(usercode: Usercode, wantsSms: Boolean)(implicit c: Connection): Unit = {
    if (!exists(usercode)) {
      save(usercode)
    }

    SQL"""UPDATE USER_PREFERENCE SET WANTS_SMS = $wantsSms WHERE USERCODE = ${usercode.string}""".execute()
  }

  override def getUserSmsNumber(usercode: Usercode)(implicit c: Connection): Option[String] = {
    SQL"SELECT SMS_NUMBER FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .as(get[Option[String]]("sms_number").singleOpt)
      .flatten
  }

  override def setUserSmsNumber(usercode: Usercode, phoneNumber: String)(implicit c: Connection): Unit = {
    if (!exists(usercode)) {
      save(usercode)
    }

    SQL"""UPDATE USER_PREFERENCE SET SMS_NUMBER = $phoneNumber, SMS_VERIFICATION = null, SMS_NUMBER_TO_VERIFY = null WHERE USERCODE = ${usercode.string}""".execute()
  }

  override def getColourSchemePreference(usercode: Usercode)(implicit c: Connection): ColourScheme = {
    SQL"SELECT CHOSEN_COLOUR_SCHEME, COLOUR_SCHEME_HIGH_CONTRAST FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .as((int("chosen_colour_scheme") ~
        bool("colour_scheme_high_contrast") map {
        case schemeId ~ highContrast => ColourScheme(schemeId, highContrast)
      }).single)
  }

  override def setColourSchemePreference(usercode: Usercode, chosenScheme: ColourScheme)(implicit c: Connection): Boolean = {
    if (!exists(usercode)) {
      save(usercode)
    }

    SQL"""UPDATE USER_PREFERENCE SET CHOSEN_COLOUR_SCHEME = ${chosenScheme.schemeId},
          COLOUR_SCHEME_HIGH_CONTRAST=${chosenScheme.highContrast} WHERE USERCODE = ${usercode.string}""".execute()
  }

  override def setUserSmsVerificationCode(usercode: Usercode, code: String)(implicit c: Connection): Boolean = {
    if (!exists(usercode)) {
      save(usercode)
    }

    SQL"""UPDATE USER_PREFERENCE SET SMS_VERIFICATION = $code WHERE USERCODE = ${usercode.string}""".execute()
  }

  override def getVerificationCode(usercode: Usercode)(implicit c: Connection): Option[String] = {
    SQL"SELECT SMS_VERIFICATION FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .as(get[Option[String]]("sms_verification").singleOpt)
      .flatten
  }

  override def setUserSmsVerificationNumber(usercode: Usercode, phoneNumber: String)(implicit c: Connection): Boolean = {
    if (!exists(usercode)) {
      save(usercode)
    }

    SQL"""UPDATE USER_PREFERENCE SET SMS_NUMBER_TO_VERIFY = $phoneNumber WHERE USERCODE = ${usercode.string}""".execute()
  }

  override def getVerificationNumber(usercode: Usercode)(implicit c: Connection): Option[String] = {
    SQL"SELECT SMS_NUMBER_TO_VERIFY FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"
      .as(get[Option[String]]("sms_number_to_verify").singleOpt)
      .flatten
  }

  override def getFeaturePreferences(usercode: Usercode)(implicit c: Connection): FeaturePreferences =
    SQL"""SELECT EAP_UNTIL FROM USER_PREFERENCE WHERE USERCODE = ${usercode.string}"""
      .as(featurePreferencesParser.singleOpt)
      .getOrElse(FeaturePreferences.empty)

  override def setFeaturePreferences(usercode: Usercode, prefs: FeaturePreferences)(implicit c: Connection): Boolean = {
    if (!exists(usercode)) {
      save(usercode)
    }

    SQL"UPDATE USER_PREFERENCE SET EAP_UNTIL = ${prefs.eapUntil.orNull[DateTime]} WHERE USERCODE = ${usercode.string}".execute()
  }
  
  private def allFeaturePreferences()(implicit c: Connection): Seq[(User, FeaturePreferences)] = {
    val usercodedPrefs = SQL"SELECT USERCODE, EAP_UNTIL FROM USER_PREFERENCE"
      .as((usercodeParser ~ featurePreferencesParser).*)
      .map {
        case usercode ~ fp => usercode -> fp
      }
    
    val allUsers = userLookupService.getUsers(usercodedPrefs.map(_._1)).getOrElse(Map.empty)
    
    usercodedPrefs.map {
      case (usercode, fp) if allUsers.isDefinedAt(usercode) => allUsers.get(usercode).get -> fp
      case (usercode, fp) => User.unknown(usercode) -> fp
      case _ => User.unknown(Usercode("-")) -> FeaturePreferences.empty
    }
  }
  
  override def countEAPByType()(implicit c: Connection): Map[String, Int] = {
    import utils.UserLookupUtils.UserStringer

    ListMap((for {
           (userType, pair) <- allFeaturePreferences().groupBy(_._1.toTypeString)
           fps = pair.map(_._2)
           eapCount = fps.filter(_.eap).length
         } yield userType -> eapCount)(collection.breakOut): _*)
  }
  
  override def countEAPByDepartment()(implicit c: Connection): Map[Option[Department], Int] = {
    ListMap((for {
      (dept, pair) <- allFeaturePreferences().groupBy(_._1.department)
      fps = pair.map(_._2)
      eapCount = fps.filter(_.eap).length
    } yield dept -> eapCount)(collection.breakOut): _*)
  }
}
