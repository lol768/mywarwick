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

    val registration = db.withConnection(implicit c =>
      pushRegistrationDao.getPushRegistrationByToken(token)
    )

    db.withTransaction { implicit c =>
      val notifications = activityDao.getNotificationsSinceDate(registration.usercode, registration.lastFetchedAt)
      pushRegistrationDao.updateLastFetched(token)
      notifications
    }
  }

  def fetchPushNotifications(token: String): JsValue = {

    val pushNotifications = getNotificationsSinceLastFetch(token).map(notification =>
      Json.obj(
        "title" -> JsString(notification.title),
        "body" -> JsString(notification.text)
      )
    )
    Json.toJson(pushNotifications)
  }
}
