package services.messaging

import com.google.inject.Inject
import models.Activity
import play.api.db._
import play.api.libs.json.{JsString, JsValue, Json}
import play.db.NamedDatabase
import services.dao.{ActivityDao, PushRegistrationDao}

class FetchNotificationsService @Inject()(
  @NamedDatabase("default") db: Database,
  pushRegistrationDao: PushRegistrationDao,
  activityDao: ActivityDao
) {

  def getNotificationsSinceLastFetch(token: String): Seq[Activity] = {

    db.withTransaction { implicit c =>
      val registration = pushRegistrationDao.getPushRegistrationByToken(token)
      val notifications = activityDao.getPushNotificationsSinceDate(registration.usercode, registration.lastFetchedAt)
      pushRegistrationDao.updateLastFetched(token)
      notifications
    }
  }

  def fetchPushNotifications(token: String): JsValue = {

    val pushNotifications = getNotificationsSinceLastFetch(token).map(notification =>
      Json.obj(
        "title" -> JsString(notification.title),
        "body" -> JsString(notification.text.orNull)
      )
    )
    Json.toJson(pushNotifications)
  }
}
