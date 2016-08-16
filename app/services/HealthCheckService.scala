package services

import akka.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import models.QueueStatus
import org.joda.time.DateTime
import services.messaging.MessagingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HealthCheckService {
  val frequency = 20.seconds
}

@Singleton
class HealthCheckService @Inject()(
  messagingService: MessagingService,
  system: ActorSystem
) {

  import HealthCheckService._

  var healthCheckLastRunAt: DateTime = null
  var messagingQueueStatus: Seq[QueueStatus] = null
  var oldestUnsentMessageCreatedAt: Option[DateTime] = None

  def runNow(): Unit = {
    healthCheckLastRunAt = DateTime.now
    messagingQueueStatus = messagingService.getQueueStatus()
    oldestUnsentMessageCreatedAt = messagingService.getOldestUnsentMessageCreatedAt()
  }

  runNow()
  system.scheduler.schedule(frequency, frequency)(runNow())

}
