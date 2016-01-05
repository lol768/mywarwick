package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import models.Activity
import models.Platform.Google
import org.joda.time.DateTime
import play.api.Configuration
import play.api.Play.current
import play.api.db._
import play.api.libs.json.{JsValue, JsObject, Json}
import play.api.libs.ws.WS
import services.ActivityService
import services.dao.{ActivityDao, PushRegistrationDao}
import warwick.sso.Usercode

import scala.concurrent.Future


class GCMOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  activityDao: ActivityDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration
) extends OutputService {

  val apiKey = configuration.getString("start.gcm.apiKey")
    .getOrElse(throw new IllegalStateException("Missing GCM API key - set start.gcm.apiKey"))

  def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    System.out.println("Send was called")
    val usercode = message.user.usercode
    db.withConnection(implicit c =>
      pushRegistrationDao.getPushRegistrationsForUser(usercode) foreach { registration =>
        sendNotification(registration.token)
      }
    )
    Future.successful(ProcessingResult(success = true, "yay"))
  }

  def subscribe(usercode: Usercode, token: String): Boolean = {
    db.withConnection(implicit c => pushRegistrationDao.saveRegistration(usercode, Google, token))
  }

  def getNotificationsSinceLastFetch(token: String): Seq[Activity] = {

    val registration = db.withConnection(implicit c =>
      pushRegistrationDao.getPushRegistrationByToken(token)
    )

    db.withConnection(implicit c =>
      activityDao.getActivitiesForUser(registration.usercode, 50, Some(DateTime.now), Some(registration.lastFetchedAt), shouldNotify = 1).map(activityResponse => {
        activityResponse.activity
      })
    )
  }

  def fetchNotifications(token: String): JsValue = {
    Json.toJson(getNotificationsSinceLastFetch(token))

    // update last check to now
  }

  def sendNotification(token: String): Unit = {
    System.out.println(s"sending notification to $token")
    val body = Json.obj(
      "to" -> token
      // This is where to put payload for native iOS and Android clients
    )

    WS.url("https://android.googleapis.com/gcm/send")
      .withHeaders(
        "Authorization" -> s"key=$apiKey",
        "Content-type" -> "application/json"
      )
      .post(body)
  }
}
