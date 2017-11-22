package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import models.MessageSend
import models.Platform.Google
import play.api.Configuration
import play.api.db._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import services.dao.PushRegistrationDao
import system.Logging
import warwick.sso.Usercode

import scala.concurrent.Future


class FCMOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration,
  ws: WSClient
) extends MobileOutputService with Logging {

  import system.ThreadPools.mobile

  private val ARROW_EMOJI = "↗️"

  val apiKey = configuration.get[Option[String]]("mywarwick.fcm.apiKey")
    .getOrElse(throw new IllegalStateException("Missing FCM API key - set mywarwick.fcm.apiKey"))

  def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    val usercode = message.user.usercode

    db.withConnection { implicit c =>
      val sendNotifications =
        pushRegistrationDao.getPushRegistrationsForUser(usercode)
          .filter(_.platform == Google)
          .map(_.token)
          .map(sendNotification(message))

      Future.sequence(sendNotifications).map(_ => ProcessingResult(success = true, "yay"))
    }
  }

  def sendNotification(message: MessageSend.Heavy)(token: String): Future[Unit] = {
    val body = Json.obj(
      "to" -> token,
      "notification" -> Json.obj(
        "title" -> JsString(message.activity.url.map(_ => s"${message.activity.title} $ARROW_EMOJI").getOrElse(message.activity.title)),
        "body" -> message.activity.text
      )
    )

    ws.url("https://fcm.googleapis.com/fcm/send")
      .addHttpHeaders(
        "Authorization" -> s"key=$apiKey",
        "Content-Type" -> "application/json"
      )
      .post(body)
      .map { response =>
        val errors = (response.json \ "results" \\ "error").map(_.as[String])

        if (errors.contains("NotRegistered")) {
          logger.info(s"Received NotRegistered FCM error, removing token=$token")
          db.withConnection { implicit c =>
            pushRegistrationDao.removeRegistration(token)
          }
        }

        if (errors.nonEmpty && !errors.forall(_ == "NotRegistered")) {
          logger.warn("FCM errors: " + errors.mkString(", "))
        }
      }
  }

  override def clearUnreadCount(user: Usercode): Unit = {
    // Not a thing on FCM, do nothing.
  }
}
