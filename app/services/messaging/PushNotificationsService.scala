package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.db._
import play.api.libs.json.Json
import play.api.libs.ws.WS
import services.dao.PushNotificationsDao

import scala.concurrent.Future

@Named("mobile")
class PushNotificationsService @Inject()(
  pushNotificationsDao: PushNotificationsDao,
  @NamedDatabase("default") db: Database
) extends OutputService {

  def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    val usercode = message.user.usercode.string
    db.withConnection(implicit c =>
      pushNotificationsDao.getRegistrationsFor(usercode) map { registration =>
        sendNotification(registration.registrationId, usercode)
      }
    )
    Future.successful(ProcessingResult(success = true, "yay"))
  }

  def subscribe(usercode: String, platform: String, regId: String): Boolean = {
    db.withConnection(implicit c => pushNotificationsDao.saveSubscription(usercode, platform, regId))
  }

  def sendNotification(regId: String, usercode: String): Unit = {
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
