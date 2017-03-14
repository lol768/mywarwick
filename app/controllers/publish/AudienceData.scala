package controllers.publish

case class AudienceData(
  audience: Seq[String],
  department: Option[String]
)
