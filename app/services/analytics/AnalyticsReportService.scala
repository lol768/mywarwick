package services.analytics

import com.google.api.services.analyticsreporting.v4.model._
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
  config: Configuration,
  analyticsTransport: AnalyticsTransport
) extends AnalyticsReportService {

  // DateRange object accepts "nDaysAgo" string for start and end dates
  private def daysAgo(num: Int) = s"${num}DaysAgo"

  //view id for guest
  val VIEW_ID_GUEST = config.getString("start.analytics.view-id.guests")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics view id - set start.analytics.view-id.guests"))

  //view id for logged in user
  val VIEW_ID_LOGGED_IN_USER = config.getString("start.analytics.view-id.users")
    .getOrElse(throw new IllegalStateException("Missing Google Analytics view id - set start.analytics.view-id.users"))


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

    val filterClause = new DimensionFilterClause()
      .setFilters(filters.asJava)

    def newReportRequest = new ReportRequest()
      .setDateRanges(java.util.Arrays.asList(dateRange))
      .setMetrics(metricsSet.asJava)
      .setDimensions(dimensionsSet.asJava)
      .setDimensionFilterClauses(java.util.Arrays.asList(filterClause))

    /*
    google does not allow getting reports for multiple requests with different ids, so we have to
    make these requests one by one
     */

    //make requests
    val requestForGuests: GetReportsRequest = new GetReportsRequest().setReportRequests(java.util.Arrays.asList(newReportRequest.setViewId(VIEW_ID_GUEST)))
    val requestForUsers: GetReportsRequest = new GetReportsRequest().setReportRequests(java.util.Arrays.asList(newReportRequest.setViewId(VIEW_ID_LOGGED_IN_USER)))

    //get results
    val resultForGuests = analyticsTransport.getReports(requestForGuests)
    val resultForLoggedInUsers = analyticsTransport.getReports(requestForUsers)

    //get reports
    val reportForGuests = resultToRows(resultForGuests)
    val reportForUsers = resultToRows(resultForLoggedInUsers)

    //extract news ids
    val allIds = (reportForGuests ++ reportForUsers).map(_.id).distinct

    //map to Seq[CombinedRow]
    allIds.map { id =>
      val guestCount = reportForGuests.find(_.id == id).map(_.count).getOrElse(0)
      val userCount = reportForUsers.find(_.id == id).map(_.count).getOrElse(0)
      CombinedRow(id, guestCount, userCount)
    }
  }

  def resultToRows(response: GetReportsResponse) = response.getReports.asScala.flatMap { report =>
    val rows = Option(report.getData.getRows)
    rows.map(_.asScala).getOrElse(Nil).map { row =>
      Row(
        row.getDimensions.get(0).split("/")(2), // get newsId substring from pagePath
        row.getMetrics.get(0).getValues.get(0).toInt
      )
    }
  }


}

