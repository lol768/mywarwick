package controllers.admin.reporting

import org.joda.time.{DateTime, Interval}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.JodaForms.jodaDate

case class DatedReportFormData(fromDate: DateTime, toDate: DateTime) {
  def interval: Interval = new Interval(fromDate, toDate)
}

object DatedReportFormData {
  def form = Form(
    mapping(
      "fromDate" -> jodaDate("yyyy-MM-dd'T'HH:mm:ss"),
      "toDate" -> jodaDate("yyyy-MM-dd'T'HH:mm:ss")
    )(DatedReportFormData.apply)(DatedReportFormData.unapply) verifying(
      """"From" date must be before "To" date""",
      data => data.fromDate.isBefore(data.toDate)
    )
  )
}
