package services.healthcheck

import com.google.inject.Inject
import org.joda.time.DateTime
import services.HealthCheckService
import services.healthcheck.HealthCheckStatus.{Critical, Okay, Warning}

class PublisherAlertFrequencyHealthCheck @Inject()(
  healthCheckService: HealthCheckService
) extends HealthCheck[Int] {

  private val tooMany = 6

  private val farTooMany = 9

  private def counts = healthCheckService.notificationCountByPublisher

  override def status: HealthCheckStatus = {
    if (criticalPublishers.nonEmpty || warningPublishers.length > 1) {
      Critical
    } else if (warningPublishers.nonEmpty) {
      Warning
    } else {
      Okay
    }
  }

  override val name = "publishing-frequency"

  private def warningPublishers = counts.filter(_.count >= tooMany)

  private def criticalPublishers = counts.filter(_.count >= farTooMany)

  override def outerMessage: String = message

  override def message: String = {
    if (warningPublishers.isEmpty) {
      "No publishers are sending too many alerts"
    } else {
      val list = warningPublishers.map(pub => s"${pub.name}: ${pub.count}").mkString(", ")

      s"$value publishers have sent $tooMany or more alerts in the last 48 hours: $list"
    }
  }

  override def perfData: Seq[PerfData[Int]] = counts.map(pub =>
    // NEWSTART-1516 "-" character breaks CheckMK perfData parsing
    PerfData(pub.id.replace("-", "_"), pub.count, Option(tooMany), Option(farTooMany))
  )

  override def value: Int = warningPublishers.length

  override val warning = 1

  override val critical = 2

  override def testedAt: DateTime = healthCheckService.healthCheckLastRunAt

}
