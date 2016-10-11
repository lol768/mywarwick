package services.messaging

import javax.inject.Inject

import actors.MessageProcessing.ProcessingResult
import models.MessageSend.Heavy
import models.Platform.WebPush
import models.messaging.Subscription
import nl.martijndwars.webpush.{Notification, PushService}
import play.api.Configuration
import play.api.db.{Database, NamedDatabase}
import play.api.libs.json.Json
import services.dao.PushRegistrationDao

import scala.concurrent.Future

class WebPushOutputService @Inject()(
  pushRegistrationDao: PushRegistrationDao,
  @NamedDatabase("default") db: Database,
  configuration: Configuration
) extends OutputService {

  import system.ThreadPools.mobile

  val apiKey = configuration.getString("mywarwick.gcm.apiKey")
    .getOrElse(throw new IllegalStateException("Missing GCM API key - set mywarwick.gcm.apiKey"))

  val pushService = new PushService
  pushService.setGcmApiKey(apiKey)

  override def send(message: Heavy) = db.withConnection { implicit c =>
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

        Future(pushService.send(notification))
      }

    Future.sequence(futures)
      .recover {
        case e: Throwable => ProcessingResult(success = false, e.toString)
      }
      .map(_ => ProcessingResult(success = true, "Web Push sent"))
  }
}

