package services.messaging

import java.io.FileInputStream
import java.util.concurrent.TimeUnit

import actors.MessageProcessing.ProcessingResult
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import javax.inject.{Inject, Named}
import models.MessageSend
import models.Platform.Google
import play.api.Configuration
import play.api.db._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import services.dao.{PublisherDao, PushRegistrationDao}
import system.Logging
import warwick.sso.Usercode

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@Named("fcm")
class FCMOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  publisherDao: PublisherDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration,
  ws: WSClient
)(implicit @Named("mobile") ec: ExecutionContext) extends MobileOutputService with Logging {

  private val FCMProjectId = configuration.getOptional[String]("mywarwick.fcm.projectId")
    .getOrElse(throw new IllegalStateException("Missing FCM config - set mywarwick.fcm.projectId"))

  private val FCMServiceAccountKeyPath = configuration.getOptional[String]("mywarwick.fcm.serviceAccountKeyPath")
    .getOrElse(throw new IllegalStateException("Missing FCM config - set mywarwick.fcm.serviceAccountKeyPath"))

  private lazy val urgentChannelId: String = configuration.getOptional[String]("mywarwick.fcm.urgentChannelId")
    .getOrElse(throw new IllegalStateException("Missing FCM config - set mywarwick.fcm.urgentChannelId"))

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
    db.withConnection { implicit c =>
      publisherDao.getProvider(message.activity.providerId) match {
        case Some(provider) if provider.overrideMuting =>
          send(message.user.usercode, MobileOutputService.toPushNotification(message.activity, Some(Priority.HIGH), Some(urgentChannelId)))
        case _ =>
          send(message.user.usercode, MobileOutputService.toPushNotification(message.activity))
      }
    }

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

    /*
     This is a "data-only" message which means our native MessageHandler
     will always handle it. Do not add any "notification" properties to
     the message. You can add more data to the "data" object as required,
     but the native app will need updating to do anything with it.
     */
    val body = Json.obj(
      "message" -> Json.obj(
        "token" -> token,
        "android" -> Json.obj(
          "ttl" -> s"${ttl.getOrElse(defaultTtl).toSeconds.toInt}s",
          "priority" -> Json.toJson(priority.getOrElse(Priority.HIGH)),
          "data" -> (Json.obj(
            "id" -> id,
            "title" -> JsString(buildTitle(Emoji.ARROW)),
            "body" -> payload.text,
            "priority" -> Json.toJson(priority.getOrElse(Priority.NORMAL))
          ) ++ notificationChannel)
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
              if (err.details.isEmpty) {
                logger.error(s"FCM Error: code=${err.code} message=${err.message} status=${err.status}")
              } else if (err.details.exists(_.errorCode.contains("UNREGISTERED"))) {
                logger.info(s"Received UNREGISTERED FCM error, removing token=$token")
                db.withConnection { implicit c =>
                  pushRegistrationDao.removeRegistration(token)
                }
              } else {
                val errorCode = err.details.find(_.errorCode.nonEmpty).flatMap(_.errorCode).map(c => s"errorCode=$c").getOrElse("[Unknown error code]")
                val fieldViolations = err.details.flatMap(_.fieldViolations).flatten.map(_.stringify).mkString(", ")
                logger.info(s"FCM response status: ${err.code} ${err.status}, $errorCode $fieldViolations")
              }
            })
          }
        )
      }
  }

  case class FCMFieldViolation(field: Option[String], description: Option[String]) {
    val stringify: String = Seq(field.orElse(Some("[Unknown field]")), description).flatten.mkString(": ")
  }
  object FCMFieldViolation {
    implicit val reads: Reads[FCMFieldViolation] = Json.reads[FCMFieldViolation]
  }

  case class FCMErrorDetails(errorCode: Option[String], fieldViolations: Option[Seq[FCMFieldViolation]])
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
