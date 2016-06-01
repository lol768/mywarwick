package controllers.admin

trait AudienceFormData {
  def audience: Seq[String]
  def department: Option[String]
}
