package services.messaging

import java.io.FileInputStream

import actors.MessageProcessing.ProcessingResult
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
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
import collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

@Named("fcm")
class FCMOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration,
  ws: WSClient
) extends MobileOutputService with Logging {

  import system.ThreadPools.mobile

  private val FCMProjectId = configuration.getOptional[String]("mywarwick.fcm.projectId")
    .getOrElse(throw new IllegalStateException("Missing FCM config - set mywarwick.fcm.projectId"))

  private val FCMServiceAccountKeyPath = configuration.getOptional[String]("mywarwick.fcm.serviceAccountKeyPath")
    .getOrElse(throw new IllegalStateException("Missing FCM config - set mywarwick.fcm.serviceAccountKeyPath"))

  private val FCMScope = "https://www.googleapis.com/auth/firebase.messaging"

  private lazy val googleCredential: GoogleCredential =
    GoogleCredential.fromStream(new FileInputStream(FCMServiceAccountKeyPath)).createScoped(Seq(FCMScope).asJava)

  private def getFCMAccessToken: String = {
    // Check if there's a valid token that expires more than a minute from now
    Option(googleCredential.getExpiresInSeconds)
      .filter(_ > 60)
      .map(_ => googleCredential.getAccessToken)
      .getOrElse {
        googleCredential.refreshToken()
        googleCredential.getAccessToken
      }
  }

  val defaultTtl: FiniteDuration = 5.minutes

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
    import pushNotification._
    val body = Json.obj(
      "message" -> Json.obj(
        "token" -> token,
        "notification" -> Json.obj(
          "title" -> JsString(buildTitle(Emoji.ARROW)),
          "body" -> payload.text
        ),
        "android" -> Json.obj(
          "ttl" -> s"${ttlSeconds.getOrElse(defaultTtl.toSeconds.toInt)}s",
          "priority" -> Json.toJson(priority.getOrElse(Priority.NORMAL)),
          "data" -> (Json.obj(
            "notification_id" -> id,
            "priority" -> Json.toJson(priority.getOrElse(Priority.NORMAL))
          ) ++ channel.map(channel => Json.obj(
            "channel" -> JsString(channel)
          )).getOrElse(JsObject(Nil))),
        )
      )
    )

    val FCMToken = getFCMAccessToken

    ws.url(s"https://fcm.googleapis.com/v1/projects/$FCMProjectId/messages:send")
      .addHttpHeaders(
        "Authorization" -> s"Bearer $FCMToken",
        "Content-Type" -> "application/json"
      )
      .post(body)
      .map { response =>
        response.json.validate[FCMResponse].fold(
          errors => {
            logger.error(s"Could not parse JSON result from FCM:")
            errors.foreach { case (path, validationErrors) =>
              logger.error(s"$path: ${validationErrors.map(_.message).mkString(", ")}")
            }
          },
          res => {
            res.error.foreach(err => logger.warn(s"FCM error: ${Json.prettyPrint(err)}"))
            if (res.error_code.contains("UNREGISTERED")) {
              logger.info(s"Received UNREGISTERED FCM error, removing token=$token")
              db.withConnection { implicit c =>
                pushRegistrationDao.removeRegistration(token)
              }
            } else {
              res.error_code.foreach(code => s"FCM error code: $code")
            }
          }
        )
      }
  }

  case class FCMResponse(name: Option[String], error: Option[JsValue], error_code: Option[String])
  object FCMResponse {
    implicit val reads: Reads[FCMResponse] = Json.reads[FCMResponse]
  }

  override def clearUnreadCount(user: Usercode): Unit = {
    // Not a thing on FCM, do nothing.
  }
}
