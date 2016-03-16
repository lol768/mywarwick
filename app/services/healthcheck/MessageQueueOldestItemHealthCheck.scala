package services.healthcheck

import com.google.inject.Inject
import org.joda.time.DateTime
import org.joda.time.DateTime.now
import services.HealthCheckService
import services.messaging.MessagingService

class MessageQueueOldestItemHealthCheck @Inject()(
  healthCheckService: HealthCheckService,
  messagingService: MessagingService
) extends HealthCheck[Long] {

  override def name = "message-queue-oldest-item"

  override def testedAt = healthCheckService.healthCheckLastRunAt

  override def value = valueMs / (60 * 1000)

  private def valueMs: Long = healthCheckService.oldestUnsentMessageCreatedAt.map(dateTimeToMsAgo).getOrElse(0)

  private def dateTimeToMsAgo(dateTime: DateTime) = now.getMillis - dateTime.getMillis

  override def message = s"Oldest unsent message $value minutes old"

  override def warning = 20

  override def critical = 30

}
