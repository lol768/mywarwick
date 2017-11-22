package services.job

import com.google.inject.Inject
import org.joda.time.DateTime
import org.quartz.{Job, JobExecutionContext}
import services.PushRegistrationService
import services.messaging.APNSProvider
import system.Logging

import scala.collection.JavaConverters._

class APNSInactiveDeviceCleanupJob @Inject()(
  pushRegistrationService: PushRegistrationService,
  apnsProvider: APNSProvider
) extends Job with Logging {

  import apnsProvider.apns

  override def execute(jobExecutionContext: JobExecutionContext): Unit = {
    val itemsDeleted = apns.getInactiveDevices.asScala.map {
      case (deviceToken, inactiveAsOf) =>
        pushRegistrationService.removeIfNotRegisteredSince(deviceToken, new DateTime(inactiveAsOf))
    }.count(_ == true)

    logger.info(s"Deleted registrations for APNS inactive devices: deleted=$itemsDeleted")
  }

}
