package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.google.inject.name.Named

import scala.concurrent.Future

@Named("mobile")
class MobileOutputService @Inject()(
  apns: APNSOutputService,
  gcm: GCMOutputService
) extends OutputService {

  import system.ThreadPools.mobile

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    System.out.println("Send called here")
    Future.sequence(Seq(apns.send(message), gcm.send(message))).map { _ => ProcessingResult(success = true, "perfect")}
  }
}
