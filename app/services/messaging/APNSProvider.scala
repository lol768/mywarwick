package services.messaging

import com.google.inject.{ImplementedBy, Inject}
import com.notnoop.apns.{ApnsService, APNS}
import play.api.Configuration

@ImplementedBy(classOf[APNSProviderImpl])
trait APNSProvider {
  val apns: ApnsService
}

class APNSProviderImpl @Inject()(configuration: Configuration) extends APNSProvider {

  private val certFile = configuration.getString("mywarwick.apns.cert.file")
    .getOrElse(throw new IllegalStateException("Missing APNs certificate file - set mywarwick.apns.cert.file"))

  private val certPassword = configuration.getString("mywarwick.apns.cert.password")
    .getOrElse(throw new IllegalStateException("Missing APNs certificate password - set mywarwick.apns.cert.password"))

  private val isProductionDestination = configuration.getBoolean("mywarwick.apns.production").getOrElse(false)

  val apns = {
    val builder = APNS.newService().withCert(certFile, certPassword)

    if (isProductionDestination)
      builder.withProductionDestination().build()
    else
      builder.withSandboxDestination().build()
  }

}
