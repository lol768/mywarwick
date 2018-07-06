package models

import org.joda.time.LocalDate
import play.api.libs.json._


case class EAPFeatureSave(
  name: String,
  startDate: Option[LocalDate],
  endDate: Option[LocalDate],
  summaryRaw: Option[String],
  feedbackUrl: Option[String]
)

object EAPFeatureRender {
  implicit val localDateWrites: Writes[LocalDate] = (o: LocalDate) =>
    JsString(DateFormats.localDate.print(o))
  val writes: OWrites[EAPFeatureRender] = Json.writes[EAPFeatureRender]
}

case class EAPFeatureRender(
  id: String,
  name: String,
  startDate: Option[LocalDate],
  endDate: Option[LocalDate],
  summary: Option[String],
  summaryRaw: Option[String],
  feedbackUrl: Option[String]
) extends Ordered[EAPFeatureRender] {
  def toSave = EAPFeatureSave(
    name = this.name,
    startDate = this.startDate,
    endDate = this.endDate,
    summaryRaw = this.summaryRaw,
    feedbackUrl = this.feedbackUrl
  )

  def available(today: LocalDate = LocalDate.now): Boolean =
    (startDate.isEmpty || startDate.get.isEqual(today) || startDate.get.isBefore(today)) &&
      (endDate.isEmpty || endDate.get.isEqual(today) || endDate.get.isAfter(today))

  private val thePast = new LocalDate(0)

  override def compare(that: EAPFeatureRender): Int =
    this.startDate.getOrElse(thePast).compareTo(that.startDate.getOrElse(thePast))
}
