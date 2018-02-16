package services.messaging

import actors.MessageProcessing
import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.google.inject.name.Named
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

@Named("fcm")
class FCMOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration,
  ws: WSClient
) extends MobileOutputService with Logging {

  import system.ThreadPools.mobile

  private val ARROW_EMOJI = "↗️"

  val apiKey: String = configuration.getOptional[String]("mywarwick.fcm.apiKey")
    .getOrElse(throw new IllegalStateException("Missing FCM API key - set mywarwick.fcm.apiKey"))

  def send(message: MessageSend.Heavy): Future[ProcessingResult] =
    send(message.user.usercode, MobileOutputService.toPushNotification(message.activity))

  def processPushNotification(usercodes: Set[Usercode], pushNotification: PushNotification): Future[ProcessingResult] =
    Future.sequence(usercodes.map(send(_, pushNotification))).map(_ => ProcessingResult(success = true, "ok"))

  def send(usercode: Usercode, pushNotification: PushNotification): Future[ProcessingResult] =
    db.withConnection { implicit c =>
      val sendNotifications =
        pushRegistrationDao.getPushRegistrationsForUser(usercode)
          .filter(_.platform == Google)
          .map(_.token)
          .map(sendNotification(pushNotification))

      Future.sequence(sendNotifications).map(_ => ProcessingResult(success = true, "yay"))
    }

  def sendNotification(pushNotification: PushNotification)(token: String): Future[Unit] = {
    val body = Json.obj(
      "to" -> token,
      "notification" -> Json.obj(
        "title" -> JsString(pushNotification.payload.url.map(_ => s"${pushNotification.payload.title} $ARROW_EMOJI").getOrElse(pushNotification.payload.title)),
        "body" -> pushNotification.payload.text
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
