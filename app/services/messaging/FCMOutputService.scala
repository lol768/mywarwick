package services.messaging

import java.io.FileInputStream
import java.util.concurrent.TimeUnit

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

import scala.collection.JavaConverters._
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

  lazy val defaultTtl: FiniteDuration = FiniteDuration(28, TimeUnit.DAYS) // later

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
    val notificationChannel: JsObject = if (channel.nonEmpty) Json.obj("android_channel_id" -> JsString(channel.get)) else JsObject(Nil)

    val body = Json.obj(
      "message" -> Json.obj(
        "token" -> token,
        // TODO: remove notication payload when we are satisfied everyone has native android app supporting data-only FCM transmission
        "notification" -> Json.obj(
          "title" -> JsString(buildTitle(Emoji.ARROW)),
          "body" -> payload.text
        ),
        "android" -> Json.obj(
          "ttl" -> s"${ttl.getOrElse(defaultTtl).toSeconds.toInt}s",
          "priority" -> Json.toJson(priority.getOrElse(Priority.NORMAL)),
          "data" -> (Json.obj(
            "id" -> id,
            "title" -> JsString(buildTitle(Emoji.ARROW)),
            "body" -> payload.text,
            "priority" -> Json.toJson(priority.getOrElse(Priority.NORMAL))
          ) ++ notificationChannel),
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
            res.error.foreach(err => {
              logger.error(s"FCM Error: code=${err.code} message=${err.message} status=${err.status}")
              err.details.flatMap(_.errorCode).map {
                case code: String if code.contains("UNREGISTERED") =>
                  logger.info(s"Received UNREGISTERED FCM error, removing token=$token")
                  db.withConnection { implicit c =>
                    pushRegistrationDao.removeRegistration(token)
                  }
                case code: String => logger.error(s"FCM response status: ${err.status}, code $code")
              }
            })
          }
        )
      }
  }

  case class FCMErrorDetails(errorCode: Option[String], fieldViolation: Option[String])
  object FCMErrorDetails {
    implicit val reads: Reads[FCMErrorDetails] = Json.reads[FCMErrorDetails]
  }

  case class FCMError(code: Int, message: String, status: String, details: Seq[FCMErrorDetails])
  object FCMError {
    implicit val reads: Reads[FCMError] = Json.reads[FCMError]
  }

  case class FCMResponse(name: Option[String], error: Option[FCMError])
  object FCMResponse {
    implicit val reads: Reads[FCMResponse] = Json.reads[FCMResponse]
  }

  override def clearUnreadCount(user: Usercode): Unit = {
    // Not a thing on FCM, do nothing.
  }
}
