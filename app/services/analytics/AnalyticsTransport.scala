package services.analytics

import java.io.File
import javax.inject.Singleton

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import com.google.api.services.analyticsreporting.v4.model._
import com.google.inject.{ImplementedBy, Inject}
import play.api.Configuration

@ImplementedBy(classOf[GoogleAnalyticsTransport])
trait AnalyticsTransport {
  def getReports(requests: GetReportsRequest): GetReportsResponse
}

@Singleton
class GoogleAnalyticsTransport @Inject()(
  config: Configuration
) extends AnalyticsTransport {


  private val APPLICATION_NAME = "Start"
  private val JSON_FACTORY = new JacksonFactory()

  private val KEY_FILE_LOCATION = config.get[Option[String]]("mywarwick.analytics.key_path")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics key file path - set mywarwick.analytics.key_path"))
  private val SERVICE_ACCOUNT_EMAIL = config.get[Option[String]]("mywarwick.analytics.account.email")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics service account id - set mywarwick.analytics.account.email"))

  private val analytics: AnalyticsReporting = {
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val credential = new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(JSON_FACTORY)
      .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
      .setServiceAccountPrivateKeyFromP12File(new File(KEY_FILE_LOCATION))
      .setServiceAccountScopes(AnalyticsReportingScopes.all())
      .build()

    new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
      .setApplicationName(APPLICATION_NAME).build()
  }

  override def getReports(request: GetReportsRequest) = {
    analytics.reports().batchGet(request).execute()
  }
}

