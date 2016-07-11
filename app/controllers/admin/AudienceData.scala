package controllers.admin

case class AudienceData(
  audience: Seq[String],
  department: Option[String]
)
