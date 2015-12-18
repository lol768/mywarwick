package services

import com.google.inject.{ImplementedBy, Inject}
import play.api.db._
import play.api.libs.json.Json
import play.api.libs.ws.WS
import services.dao.PushNotificationsDao

@ImplementedBy(classOf[PushNotificationsServiceImpl])
trait PushNotificationsService {
  def subscribe(usercode: String, platform: String, regId: String): Boolean
  def sendNotification(regId: String, usercode: String): Unit
}

// TODO: should extend OutputService
class PushNotificationsServiceImpl @Inject()(
  pushNotificationsDao: PushNotificationsDao,
  @NamedDatabase("default") db: Database
) extends PushNotificationsService {

  override def subscribe(usercode: String, platform: String, regId: String): Boolean = {
    db.withConnection(implicit c => pushNotificationsDao.saveSubscription(usercode, platform, regId))
  }

  override def sendNotification(regId: String, usercode: String): Unit = {
    import play.api.Play.current
    val gcmApiKey = current.configuration.getString("gcm.apiKey").get

    val body = Json.obj(
      "to" -> regId,
      "notification" -> Json.obj(
        "title" -> s"Hello ${usercode}",
        "body" -> "This is your first notification",
        "icon" -> ""
      ),
      "data" -> Json.obj(
        "title" -> s"Hello ${usercode}",
        "body" -> "This is your first notification",
        "icon" -> ""
      )
    )

    WS.url("https://android.googleapis.com/gcm/send")
      .withHeaders(
        "Authorization" -> s"key=${gcmApiKey}",
        "Content-type" -> "application/json"
      )
      .post(body)
  }
}
