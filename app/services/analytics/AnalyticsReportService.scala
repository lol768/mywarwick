package services.analytics

import java.io.File
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.analyticsreporting.v4.model._
import com.google.api.services.analyticsreporting.v4.{AnalyticsReporting, AnalyticsReportingScopes}
import com.google.inject.{ImplementedBy, Inject}
import play.api.Configuration

import scala.collection.JavaConverters._

/*
id and clicks count
 */
case class Row(id: String, count: Int)

/*
combined clicks information from guest clicks and logged in users' clicks
 */
case class CombinedRow(id: String, guests: Int, users: Int)

@ImplementedBy(classOf[AnalyticsReportServiceImpl])
trait AnalyticsReportService {
  protected val START_DATE = "2016-01-07"
  // dodgy af (in absence of 'all-of-time' option)
  protected val TODAY = "today"

  def idEqualityFilter(ids: Seq[String], dimension: String): Seq[DimensionFilter]

  def getReport(
    ids: Seq[String],
    metrics: Seq[String],
    dimensions: Seq[String],
    filters: Seq[DimensionFilter],
    startDate: String = START_DATE,
    endDate: String = TODAY): Seq[CombinedRow]

}

class AnalyticsReportServiceImpl @Inject()(
  config: Configuration
) extends AnalyticsReportService {

  // DateRange object accepts "nDaysAgo" string for start and end dates
  private def daysAgo(num: Int) = s"${num}DaysAgo"

  private val APPLICATION_NAME = "Start"
  private val JSON_FACTORY = new JacksonFactory()

  private val KEY_FILE_LOCATION = config.getString("start.analytics.key_path")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics key file path - set start.analytics.key_path"))
  private val SERVICE_ACCOUNT_EMAIL = config.getString("start.analytics.account.email")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics service account id - set start.analytics.account.email"))

  //view id for guest
  private val VIEW_ID_GUEST = config.getString("start.analytics.view-id.guests")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics view id - set start.analytics.view_id_guest"))

  //view id for logged in user
  private val VIEW_ID_LOGGED_IN_USER = config.getString("start.analytics.view-id.users")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics view id - set start.analytics.view_id_logged_in_user"))


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

  private def ga(str: String) = s"ga:$str"

  override def idEqualityFilter(ids: Seq[String], dimension: String) =
    ids.map(id => new DimensionFilter()
      .setDimensionName(ga(dimension))
      .setOperator("EXACT")
      .setExpressions(List(id).asJava)
    )

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

    def newReportRequest = new ReportRequest()
      .setDateRanges(java.util.Arrays.asList(dateRange))
      .setMetrics(metricsSet.asJava)
      .setDimensions(dimensionsSet.asJava)
      .setDimensionFilterClauses(java.util.Arrays.asList(filterClaus))


    def resultToRows(response: GetReportsResponse) = response.getReports.asScala.flatMap { report =>
      val rows = Option(report.getData.getRows)
      rows.map(_.asScala).getOrElse(Nil).map { row =>
        Row(
          row.getDimensions.get(0).split("/")(2), // get newsId substring from pagePath
          row.getMetrics.get(0).getValues.get(0).toInt
        )
      }
    }

    /*
    google does not allow getting reports for multiple requests, so we have to get these reports
    one by one

    the process is like request -> result -> report
     */

    //make requests
    val requestForGuests: GetReportsRequest = new GetReportsRequest().setReportRequests(java.util.Arrays.asList(newReportRequest.setViewId(VIEW_ID_GUEST)))
    val requestForUsers: GetReportsRequest = new GetReportsRequest().setReportRequests(java.util.Arrays.asList(newReportRequest.setViewId(VIEW_ID_LOGGED_IN_USER)))

    //get results
    val resultForGuests = analytics.reports().batchGet(requestForGuests).execute()
    val resultForLoggedInUsers = analytics.reports().batchGet(requestForUsers).execute()

    //get reports
    val reportForGuests = resultToRows(resultForGuests)
    val reportForUsers = resultToRows(resultForLoggedInUsers)

    //extract news ids
    val allIds = (reportForGuests ++ reportForUsers).map(_.id).distinct

    //map to Seq[CombinedRow]
    allIds.map { id =>
      val guestCount =  reportForGuests.find(_.id == id).map(_.count).getOrElse(0)
      val userCount = reportForUsers.find(_.id == id).map(_.count).getOrElse(0)
      CombinedRow(id, guestCount, userCount)
    }
  }


}

