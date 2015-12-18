package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.google.inject.name.Named
import com.notnoop.apns.APNS
import models.Activity
import play.api.Configuration
import play.api.db.{Database, NamedDatabase}
import services.dao.PushRegistrationDao
import services.messaging.MessageSend.Heavy

import scala.concurrent.Future

@Named("apns")
class APNSOutputService @Inject()(
  @NamedDatabase("default") db: Database,
  pushRegistrationDao: PushRegistrationDao,
  configuration: Configuration
) extends OutputService {

  import system.ThreadPools.mobile

  val certFile = configuration.getString("start.apns.cert.file")
    .getOrElse(throw new IllegalStateException("Missing APNs certificate file - set start.apns.cert.file"))

  val certPassword = configuration.getString("start.apns.cert.password")
    .getOrElse(throw new IllegalStateException("Missing APNs certificate password - set start.apns.cert.password"))

  val isProductionDestination = configuration.getBoolean("start.apns.production").getOrElse(false)

  val apns = {
    val builder = APNS.newService().withCert(certFile, certPassword)

    if (isProductionDestination)
      builder.withProductionDestination().build()
    else
      builder.withSandboxDestination().build()
  }

  override def send(message: Heavy): Future[ProcessingResult] = Future {
    import message._

    val payload = makePayload(activity)

    db.withConnection { implicit c =>
      val appleDevices = pushRegistrationDao.getPushRegistrationsForUser(user.usercode.string)
        .filter(_.platform == "a")

      appleDevices.foreach(device => apns.push(device.token, payload))

      ProcessingResult(success = true, message = s"Push notification sent to ${appleDevices.length} device(s)")
    }
  }

  def makePayload(activity: Activity): String = {
    APNS.newPayload()
      .alertBody(s"${activity.providerId}: ${activity.title}")
      .sound("default")
      .build()
  }
}
