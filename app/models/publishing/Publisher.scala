package models.publishing

case class PublisherSave(
  name: String,
  maxRecipients: Option[Int] = None
)

case class Publisher(
  id: String,
  name: String,
  maxRecipients: Option[Int] = None
)

case class PublisherActivityCount(id: String, name: String, count: Int)
