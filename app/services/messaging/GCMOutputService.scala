package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import models.Platform.Google
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
        sendGCMNotification(registration.token)
      }
    )
    Future.successful(ProcessingResult(success = true, "yay"))
  }

  def subscribe(usercode: Usercode, token: String): Boolean = {
    db.withConnection(implicit c => pushRegistrationDao.saveRegistration(usercode, Google, token))
  }

  def sendGCMNotification(token: String): Unit = {
    val body = Json.obj(
      "to" -> token
      // This is where to put payload readable by native Android clients
    )

    WS.url("https://android.googleapis.com/gcm/send")
      .withHeaders(
        "Authorization" -> s"key=$apiKey",
        "Content-type" -> "application/json"
      )
      .post(body)
  }
}
