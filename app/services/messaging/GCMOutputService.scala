package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import play.api.Configuration
import play.api.Play.current
import play.api.db._
import play.api.libs.json._
import play.api.libs.ws.WS
import services.dao.PushRegistrationDao
import system.Logging

import scala.concurrent.Future


class GCMOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration
) extends OutputService with Logging {

  import system.ThreadPools.mobile

  val apiKey = configuration.getString("start.gcm.apiKey")
    .getOrElse(throw new IllegalStateException("Missing GCM API key - set start.gcm.apiKey"))

  def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    val usercode = message.user.usercode

    db.withConnection { implicit c =>
      val sendNotifications = pushRegistrationDao.getPushRegistrationsForUser(usercode).map(_.token).map(sendGCMNotification)

      Future.sequence(sendNotifications).map(_ => ProcessingResult(success = true, "yay"))
    }
  }

  def sendGCMNotification(token: String): Future[Unit] = {
    val body = Json.obj(
      "to" -> token
      // This is where to put payload readable by native Android clients
    )

    WS.url("https://android.googleapis.com/gcm/send")
      .withHeaders(
        "Authorization" -> s"key=$apiKey",
        "Content-Type" -> "application/json"
      )
      .post(body)
      .map { response =>
        val errors = (response.json \ "results" \\ "error").map(_.asOpt[String])

        if (errors.contains(Some("NotRegistered"))) {
          logger.info(s"Received NotRegistered GCM error, removing token=$token")
          db.withConnection { implicit c =>
            pushRegistrationDao.removeRegistration(token)
          }
        }
      }
  }
}
