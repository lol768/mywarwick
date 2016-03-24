package services.healthcheck

import com.google.inject.Inject
import models.MessageState.Available
import services.HealthCheckService

class MessageQueueLengthHealthCheck @Inject()(
  healthCheckService: HealthCheckService
) extends HealthCheck[Int] {

  def queueAvailableStatus = healthCheckService.messagingQueueStatus.filter(_.state == Available)

  override val name = "message-queue-length"

  override def message = s"$value messages in queue"

  override def perfData = queueAvailableStatus.map(item => PerfData(s"queue_${item.output.name.toLowerCase()}", item.count))

  override def value = queueAvailableStatus.map(_.count).sum

  override val warning = 100

  override val critical = 300

  override def testedAt = healthCheckService.healthCheckLastRunAt

}
