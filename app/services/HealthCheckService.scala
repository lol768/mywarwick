package services

import akka.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import models.QueueStatus
import org.joda.time.DateTime
import services.messaging.MessagingService
import system.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HealthCheckService {
  val defaultFrequency: FiniteDuration = 20.seconds
}

@Singleton
class HealthCheckService @Inject()(
  messagingService: MessagingService,
  system: ActorSystem
) extends Logging {

  def frequency: FiniteDuration = HealthCheckService.defaultFrequency

  var healthCheckLastRunAt: DateTime = null
  var messagingQueueStatus: Seq[QueueStatus] = null
  var oldestUnsentMessageCreatedAt: Option[DateTime] = None

  def runNow(): Unit = try {
    healthCheckLastRunAt = DateTime.now
    messagingQueueStatus = messagingService.getQueueStatus()
    oldestUnsentMessageCreatedAt = messagingService.getOldestUnsentMessageCreatedAt()
  } catch {
    case e: Exception => logger.error("Error updating health checks", e)
  }

  system.scheduler.schedule(0.seconds, frequency)(runNow())

}
