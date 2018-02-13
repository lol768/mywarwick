package actors

import javax.inject.Inject

import actors.MessageProcessing.WorkAvailable
import akka.actor._
import play.api.Configuration
import services.messaging.MessagingService
import warwick.sso.{UserLookupService}
import scala.concurrent.duration._

import scala.util.{Failure, Success, Try}

class WorkerActor @Inject()(
  config: Configuration,
  messaging: MessagingService,
  userLookup: UserLookupService
) extends Actor with ActorLogging {

  // There might not be work, but let's be optimistic
  self ! WorkAvailable

  // This chap can be our threadpool
  import context.dispatcher

  log.debug("WorkerActor created")

  def receive = {
    case WorkAvailable =>
      log.debug("Checking for work")
      messaging.lockRecord() match {
        case Some(message) =>
          log.debug("Got work!")
          val processing = messaging.processNow(message)
          processing.onComplete {
            case Success(res) if res.success => messaging.success(message)
            case Success(res) if res.error.exists(MessageProcessing.skippableErrors.contains(_)) =>
              log.warning(s"Message failed to send: ${res.message}")
              messaging.skipped(message)
            case Success(res) =>
              log.warning(s"Message failed to send: ${res.message}")
              messaging.failure(message)
            case Failure(ex) =>
              log.error(s"Message-sending threw an exception", ex)
              messaging.failure(message)
          }
          processing.onComplete {

            // Assume there's more work after this, until we find there isn't
            case _ => self ! WorkAvailable
          }
        case None =>
          // Instead of polling we should react to a nudge event from the API,
          // then this poll could be much less frequent (or never, if we're feeling
          // brave)
          // Usually this means there's no work, but can also be when there's a lot
          // of contention for work.
          log.debug("Couldn't lock any work. Sleeping for a bit.")
          context.system.scheduler.scheduleOnce(5.seconds, self, WorkAvailable)
      }
  }

}