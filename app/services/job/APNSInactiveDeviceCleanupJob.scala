package services.job

import com.google.inject.Inject
import org.joda.time.DateTime
import org.quartz.{Job, JobExecutionContext}
import services.PushRegistrationService
import services.messaging.APNSOutputService
import system.Logging

import scala.collection.JavaConversions._

class APNSInactiveDeviceCleanupJob @Inject()(
  pushRegistrationService: PushRegistrationService,
  apnsOutputService: APNSOutputService
) extends Job with Logging {

  import apnsOutputService.apns

  override def execute(jobExecutionContext: JobExecutionContext): Unit = {
    val itemsDeleted = apns.getInactiveDevices.map {
      case (deviceToken, inactiveAsOf) =>
        pushRegistrationService.removeIfNotRegisteredSince(deviceToken, new DateTime(inactiveAsOf))
    }.count(_ == true)

    logger.info(s"Deleted registrations for APNS inactive devices: deleted=$itemsDeleted")
  }

}
