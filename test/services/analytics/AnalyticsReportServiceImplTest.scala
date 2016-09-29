package services.analytics

import com.google.api.services.analyticsreporting.v4.model._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.collection.JavaConverters._


class AnalyticsReportServiceImplTest extends PlaySpec with MockitoSugar {

  class Scope {
    val analyticsTransport = mock[AnalyticsTransport]
    val config = Configuration (
      "start.analytics.view-id.guests" -> "fakeid1",
      "start.analytics.view-id.users" -> "fakeid2"
    )

    val service = new AnalyticsReportServiceImpl(
      config,
      analyticsTransport
    )


    val row1 = new ReportRow
    val row1Dimensions = Seq("/news/04cb5644-e32c-46b2-920d-98811e48ecb8/redirect")
    val row1DataRangeValues = new DateRangeValues
    row1DataRangeValues.setValues(Seq("10").asJava) //10 clicks
    row1.setDimensions(row1Dimensions.asJava)
    row1.setMetrics(Seq(row1DataRangeValues).asJava)


    val row2 = new ReportRow
    val row2Dimensions = Seq("/news/183315a0-ff9a-42c2-a7ae-0f79b914c46f/redirect")
    val row2DataRangeValues = new DateRangeValues
    row2DataRangeValues.setValues(Seq("4").asJava) //4 clicks
    row2.setDimensions(row2Dimensions.asJava)
    row2.setMetrics(Seq(row2DataRangeValues).asJava)

    val reportRowList = Seq[ReportRow](
      row1,
      row2
    )

    val reportData = new ReportData
    reportData.setRows(reportRowList.asJava)

    val report = new Report
    report.setData(reportData)


    val reports = Seq(report)
    val getReportsResponse = new GetReportsResponse
    getReportsResponse.setReports(reports.asJava)

    when(analyticsTransport.getReports(any())).thenReturn(getReportsResponse)
  }


  "AnalyticsReportServiceImplTest" should {

    "throw exception if config for GA view ids are missing" in {
      intercept[IllegalStateException] {
        val service = new AnalyticsReportServiceImpl(Configuration(), null)
      }
    }

    "getReport method should return expected result" in new Scope {
      val expected = Seq(
        CombinedRow("04cb5644-e32c-46b2-920d-98811e48ecb8",10,10),
        CombinedRow("183315a0-ff9a-42c2-a7ae-0f79b914c46f",4,4)
      )
      service.getReport(
        Nil,
        Nil,
        Nil,
        Nil,
        "",
        ""
      ) mustBe expected
    }


  }
}
