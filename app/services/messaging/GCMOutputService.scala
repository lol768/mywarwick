package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import models.Activity
import models.Platform.Google
import org.joda.time.DateTime
import play.api.Configuration
import play.api.Play.current
import play.api.db._
import play.api.libs.json._
import play.api.libs.ws.WS
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
      activityDao.getNotificationsSinceDate(registration.usercode, registration.lastFetchedAt).map(activity => {
        activity
      })
    )
  }

  def fetchNotifications(token: String): JsValue = {
    //    val list = test.map(notification =>
    //    val test = Seq(
    //      Activity("1", "tabula", "coursework due", "This is title", "This is text", None, DateTime.now, DateTime.now, shouldNotify = true),
    //      Activity("12", "tabula", "coursework due", "This is title", "This is text2", None, DateTime.now, DateTime.now, shouldNotify = true)
    //    )


    val list = getNotificationsSinceLastFetch(token).map(notification =>
      Json.obj(
        "title" -> JsString(notification.title),
        "body" -> JsString(notification.text)
      )
    )
    list.foldLeft(JsArray())((acc, x) => acc ++ Json.arr(x))

    // update last check to now
  }

  def sendNotification(token: String): Unit = {
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
