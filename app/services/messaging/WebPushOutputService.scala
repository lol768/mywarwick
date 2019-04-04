package services.messaging

import javax.inject.{Inject, Named}
import actors.MessageProcessing.ProcessingResult
import models.MessageSend.Heavy
import models.Platform.WebPush
import models.messaging.Subscription
import nl.martijndwars.webpush.{Notification, PushService, Utils}
import play.api.Configuration
import play.api.db.{Database, NamedDatabase}
import play.api.libs.json.Json
import services.dao.PushRegistrationDao
import system.Logging

import scala.concurrent.{ExecutionContext, Future}

class WebPushOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration
)(implicit @Named("mobile") ec: ExecutionContext) extends OutputService with Logging {

  private val vapidSubject = configuration.get[String]("mywarwick.vapid.subject")
  private val vapidPrivateKey = configuration.get[String]("mywarwick.vapid.privateKey")
  private val vapidPublicKey = configuration.get[String]("mywarwick.vapid.publicKey")

  import org.bouncycastle.jce.provider.BouncyCastleProvider
  import java.security.Security

  Security.addProvider(new BouncyCastleProvider)
  val pushService = new PushService
  pushService.setSubject(vapidSubject)
  pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey))
  pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey))

  override def send(message: Heavy): Future[ProcessingResult] = db.withConnection { implicit c =>
    val futures = pushRegistrationDao.getPushRegistrationsForUser(message.user.usercode)
      .filter(_.platform == WebPush)
      .flatMap(registration => Json.parse(registration.token).validate[Subscription].asOpt)
      .map { subscription =>
        val notification = new Notification(
          subscription.endpoint,
          subscription.publicKey,
          subscription.authAsBytes,
          Json.toJson(message.activity).toString.getBytes
        )

        Future {
          val response = pushService.send(notification)

          val status = response.getStatusLine
          val statusCode = status.getStatusCode

          if (statusCode < 200 || statusCode > 299) {
            logger.info(s"Error sending Web Push notification to endpoint ${subscription.endpoint}: $status")
            throw new Exception("Error status " + status)
          }
        }
      }

    Future.sequence(futures)
      .recover {
        case e: Throwable => ProcessingResult(success = false, e.toString)
      }
      .map(_ => ProcessingResult(success = true, "Web Push sent"))
  }
}

