package services.healthcheck

import com.google.inject.Inject
import models.MessageState.Failure
import services.HealthCheckService

class FailedMessageSendHealthCheck @Inject()(
  healthCheckService: HealthCheckService
) extends HealthCheck[Int] {

  def queueFailureStatus = healthCheckService.messagingQueueStatus.filter(_.state == Failure)

  override val name = "failed-message-send"

  override def message = s"$value messages failed to send in the last 24 hours"

  override def perfData = queueFailureStatus.map(item => PerfData(s"failed_${item.output.name.toLowerCase()}", item.count))

  override def value = queueFailureStatus.map(_.count).sum

  override val warn = 50

  override val critical = 100

  override def testedAt = healthCheckService.healthCheckLastRunAt

}
