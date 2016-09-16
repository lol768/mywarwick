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
    startDate: String = START_DATE,
    endDate: String = TODAY): GetReportsResponse
}

class AnalyticsReportServiceImpl @Inject()(
  config: Configuration
) extends AnalyticsReportService {

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
    startDate: String = START_DATE,
    endDate: String = TODAY) = {

    val dateRange = new DateRange()
    dateRange.setStartDate("2016-08-15")
    dateRange.setEndDate("2016-09-15")

    val metric = new Metric()
      .setExpression(ga("uniqueEvents"))

    val dimension = new Dimension()
      .setName(ga("eventLabel"))

    val filter = new DimensionFilter()
      .setDimensionName(ga("eventLabel"))
      .setOperator("EXACT")
      .setExpressions(ids.asJava)

    val filterClause = new DimensionFilterClause()
      .setFilters(List(filter).asJava)

    val request = new ReportRequest()
      .setViewId(VIEW_ID)
      //      .setDateRanges(java.util.Arrays.asList(dateRange))
      .setMetrics(java.util.Arrays.asList(metric))
      .setDimensions(java.util.Arrays.asList(dimension))
      .setDimensionFilterClauses(List(filterClause).asJava)

    val getReport: GetReportsRequest = new GetReportsRequest().setReportRequests(java.util.Arrays.asList(request))

    analytics.reports().batchGet(getReport).execute()
  }
}
