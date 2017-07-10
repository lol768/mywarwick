package models.publishing

case class Publisher(
  id: String,
  name: String,
  maxRecipients: Option[Int] = None
)
