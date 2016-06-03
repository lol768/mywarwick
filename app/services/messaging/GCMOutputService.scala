package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import models.MessageSend
import models.Platform.Google
import play.api.Configuration
import play.api.db._
import play.api.libs.json._
import play.api.libs.ws.WSAPI
import services.dao.PushRegistrationDao
import system.Logging
import warwick.sso.Usercode

import scala.concurrent.Future


class GCMOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration,
  ws: WSAPI
) extends MobileOutputService with Logging {

  import system.ThreadPools.mobile

  val apiKey = configuration.getString("start.gcm.apiKey")
    .getOrElse(throw new IllegalStateException("Missing GCM API key - set start.gcm.apiKey"))

  def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    val usercode = message.user.usercode

    db.withConnection { implicit c =>
      val sendNotifications = pushRegistrationDao.getPushRegistrationsForUser(usercode).filter(_.platform == Google).map(_.token).map(sendGCMNotification)

      Future.sequence(sendNotifications).map(_ => ProcessingResult(success = true, "yay"))
    }
  }

  def sendGCMNotification(token: String): Future[Unit] = {
    val body = Json.obj(
      "to" -> token
      // This is where to put payload readable by native Android clients
    )

    ws.url("https://android.googleapis.com/gcm/send")
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

  override def clearUnreadCount(user: Usercode): Unit = {
    // Not a thing on GCM, do nothing.
  }
}
