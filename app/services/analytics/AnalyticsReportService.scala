package services.analytics

import java.io.File

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.analyticsreporting.v4.model._
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.Configuration

import scala.collection.JavaConverters._

@ImplementedBy(classOf[AnalyticsReportServiceImpl])
trait AnalyticsReportService {
  protected val START_DATE = "2016-01-07" // dodgy af (in absence of 'all-of-time' option)
  protected val TODAY = "today"

  def getReport(
    ids: Seq[String],
    metrics: Seq[String],
    dimensions: Seq[String],
    filters: Seq[DimensionFilter],
    startDate: String = START_DATE,
    endDate: String = TODAY): GetReportsResponse
}

class AnalyticsReportServiceImpl @Inject()(
  config: Configuration
) extends AnalyticsReportService {

  // DateRange object accepts "nDaysAgo" string for start and end dates
  private def daysAgo(num: Int) = s"${num}DaysAgo"

  private lazy val APPLICATION_NAME = "Start"
  private lazy val JSON_FACTORY = new JacksonFactory()

  private lazy val KEY_FILE_LOCATION = config.getString("start.analytics.keyFilePath")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics key file path - set start.analytics.keyFilePath"))
  private lazy val SERVICE_ACCOUNT_EMAIL = config.getString("start.analytics.account.email")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics service account id - set start.analytics.account.email"))
  private lazy val VIEW_ID = config.getString("start.analytics.viewId")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics view id - set start.analytics.viewId"))

  private lazy val analytics: AnalyticsReporting = {
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

  private def ga(str: String) = s"ga:$str"

  override def getReport(
    ids: Seq[String],
    metrics: Seq[String],
    dimensions: Seq[String],
    filters: Seq[DimensionFilter],
    startDate: String,
    endDate: String) = {

    val dateRange = new DateRange()
    dateRange.setStartDate(startDate)
    dateRange.setEndDate(endDate)

    val metricsSet = metrics.map(str => new Metric().setExpression(ga(str)))

    val dimensionsSet = dimensions.map(str => new Dimension().setName(ga(str)))

    val filterClaus = new DimensionFilterClause() // ho, ho, ho, merry filtering!
      .setFilters(filters.asJava)

    val request = new ReportRequest()
      .setViewId(VIEW_ID)
      .setDateRanges(java.util.Arrays.asList(dateRange))
      .setMetrics(metricsSet.asJava)
      .setDimensions(dimensionsSet.asJava)
      .setDimensionFilterClauses(java.util.Arrays.asList(filterClaus))

    val getReport: GetReportsRequest = new GetReportsRequest().setReportRequests(java.util.Arrays.asList(request))

    analytics.reports().batchGet(getReport).execute()
  }
}
