package services.analytics

import java.util.Arrays.asList

import com.google.api.services.analyticsreporting.v4.model._
import helpers.BaseSpec
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration

import scala.collection.JavaConverters._


class AnalyticsReportServiceImplTest extends BaseSpec with MockitoSugar {

  def newReportRow(newsId: String, count: Int): ReportRow =
    new ReportRow()
      .setDimensions(asList(s"/news/$newsId/redirect"))
      .setMetrics(asList(new DateRangeValues().setValues(asList(count.toString))))

  def newReport(rows: Seq[ReportRow]): Report =
    new Report().setData(new ReportData().setRows( rows.asJava ))

  class Scope {
    val transport = mock[AnalyticsTransport]
    val config = Configuration (
      "mywarwick.analytics.view-id.guests" -> "fakeid1",
      "mywarwick.analytics.view-id.users" -> "fakeid2"
    )
    val service = new AnalyticsReportServiceImpl(config, transport)

    val row1 = newReportRow("04cb5644-e32c-46b2-920d-98811e48ecb8", 10)
    val row2 = newReportRow("183315a0-ff9a-42c2-a7ae-0f79b914c46f", 4)
    val report = newReport(rows = Seq(row1, row2))
    val getReportsResponse = new GetReportsResponse().setReports(asList(report))

    when(transport.getReports(any())).thenReturn(getReportsResponse)
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
