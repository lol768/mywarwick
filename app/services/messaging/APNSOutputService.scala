package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.notnoop.apns.APNS
import models.{MessageSend, Activity}
import models.Platform._
import play.api.Configuration
import play.api.db.{Database, NamedDatabase}
import services.dao.PushRegistrationDao

import scala.concurrent.Future

class APNSOutputService @Inject()(
  @NamedDatabase("default") db: Database,
  apnsProvider: APNSProvider,
  pushRegistrationDao: PushRegistrationDao
) extends OutputService {

  import system.ThreadPools.mobile
  import apnsProvider.apns

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = Future {
    import message._

    val payload = makePayload(activity)

    db.withConnection { implicit c =>
      val appleDevices = pushRegistrationDao.getPushRegistrationsForUser(user.usercode)
        .filter(_.platform == Apple)

      appleDevices.foreach(device => apns.push(device.token, payload))

      ProcessingResult(success = true, message = s"Push notification sent to ${appleDevices.length} device(s)")
    }
  }

  def makePayload(activity: Activity): String = {
    APNS.newPayload()
      .alertBody(activity.title)
      .sound("default")
      .build()
  }
}
