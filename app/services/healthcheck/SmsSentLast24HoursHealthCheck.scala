package services.healthcheck

import com.google.inject.Inject
import models.MessageState.Available
import services.HealthCheckService

class SmsSentLast24HoursHealthCheck @Inject()(
  healthCheckService: HealthCheckService
) extends HealthCheck[Int] {

  override val name = "sms-sent-last-24-hours"

  override def message = s"$value SMS sent in last 24 hours"

  override def value = healthCheckService.smsSentLast24Hours

  override val warning = 1000

  override val critical = 1500

  override def testedAt = healthCheckService.healthCheckLastRunAt

}
