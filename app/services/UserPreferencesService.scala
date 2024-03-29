package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import controllers.api.ColourScheme
import models.FeaturePreferences
import play.api.cache.{Cached, SyncCacheApi}
import play.api.db.{Database, NamedDatabase}
import play.api.libs.json.JsObject
import services.dao.UserPreferencesDao
import warwick.sso.Usercode

import scala.concurrent.duration._

@ImplementedBy(classOf[UserPreferencesServiceImpl])
trait UserPreferencesService {

  def exists(usercode: Usercode): Boolean

  def save(usercode: Usercode): Unit

  def countInitialisedUsers(usercodes: Set[Usercode]): Int

  def getNotificationFilter(usercode: Usercode): JsObject

  def getActivityFilter(usercode: Usercode): JsObject

  def setNotificationFilter(usercode: Usercode, filter: JsObject): Unit

  def setActivityFilter(usercode: Usercode, filter: JsObject): Unit

  def getChosenColourScheme(usercode: Usercode): ColourScheme

  def setChosenColourScheme(usercode: Usercode, chosenScheme: ColourScheme): Unit

  def getFeaturePreferences(usercode: Usercode): FeaturePreferences

  def setFeaturePreferences(usercode: Usercode, prefs: FeaturePreferences): Unit

}

@Singleton
class UserPreferencesServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: UserPreferencesDao
) extends UserPreferencesService {

  override def exists(usercode: Usercode): Boolean = db.withConnection(implicit c => dao.exists(usercode))

  override def save(usercode: Usercode): Unit = db.withConnection(implicit c => dao.save(usercode))

  override def countInitialisedUsers(usercodes: Set[Usercode]): Int =
    db.withConnection(implicit c => dao.countInitialisedUsers(usercodes))

  override def getNotificationFilter(usercode: Usercode): JsObject =
    db.withConnection(implicit c => dao.getNotificationFilter(usercode))

  override def getActivityFilter(usercode: Usercode): JsObject =
    db.withConnection(implicit c => dao.getActivityFilter(usercode))

  override def setNotificationFilter(usercode: Usercode, filter: JsObject): Unit =
    db.withConnection(implicit c => dao.setNotificationFilter(usercode, filter))

  override def setActivityFilter(usercode: Usercode, filter: JsObject): Unit =
    db.withConnection(implicit c => dao.setActivityFilter(usercode, filter))

  override def getChosenColourScheme(usercode: Usercode): ColourScheme =
    db.withConnection(implicit c => dao.getColourSchemePreference(usercode))

  override def setChosenColourScheme(usercode: Usercode, chosenScheme: ColourScheme): Unit =
    db.withConnection(implicit c => dao.setColourSchemePreference(usercode, chosenScheme))

  override def getFeaturePreferences(usercode: Usercode): FeaturePreferences =
    db.withConnection(implicit c => dao.getFeaturePreferences(usercode))

  override def setFeaturePreferences(usercode: Usercode, prefs: FeaturePreferences): Unit =
    db.withConnection(implicit c => dao.setFeaturePreferences(usercode, prefs))
}
