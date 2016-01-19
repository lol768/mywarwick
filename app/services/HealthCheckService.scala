package services

import com.google.inject.{Singleton, Inject}
import models.QueueStatus
import org.joda.time.DateTime
import play.api.Play.current
import play.api.libs.concurrent.Akka
import services.messaging.MessagingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HealthCheckService {
  val frequency = 20.seconds
}

@Singleton
class HealthCheckService @Inject()(
  messagingService: MessagingService
) {

  import HealthCheckService._

  var healthCheckLastRunAt: DateTime = null
  var messagingQueueStatus: Seq[QueueStatus] = null

  def runNow(): Unit = {
    healthCheckLastRunAt = new DateTime
    messagingQueueStatus = messagingService.getQueueStatus()
  }

  runNow()
  Akka.system.scheduler.schedule(frequency, frequency)(runNow())

}
