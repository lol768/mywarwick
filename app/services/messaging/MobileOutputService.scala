package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.{ImplementedBy, Inject}
import com.google.inject.name.Named
import models.MessageSend
import warwick.sso.Usercode

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[MobileOutputServiceImpl])
trait MobileOutputService extends OutputService {
  def clearUnreadCount(user: Usercode): Unit
}

@Named("mobile")
class MobileOutputServiceImpl @Inject()(
  apns: APNSOutputService,
  gcm: GCMOutputService
) extends MobileOutputService {

  import system.ThreadPools.mobile

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    Future.sequence(Seq(apns.send(message), gcm.send(message))).map { _ => ProcessingResult(success = true, "perfect")}
  }

  override def clearUnreadCount(user: Usercode): Unit = {
    val doneApns = Try { apns.clearUnreadCount(user) }
    val doneGcm = Try { gcm.clearUnreadCount(user) }
    doneApns.get
    doneGcm.get
  }
}
