package models.news

import org.joda.time.DateTime
import uk.ac.warwick.util.web.Uri

// unused
case class Publisher(name: String, department: Option[String], users: Seq[PublisherUser])

trait PermissionLevel
object PermissionLevel {
  case object Contributor extends PermissionLevel
  case object Admin extends PermissionLevel
}

case class PublisherUser(usercode: String, permission: PermissionLevel)

case class Link(text: String, href: Uri)

case class NewsItemRender (
  id: String,
  title: String,
  text: String,
  link: Option[Link],
  publishDate: DateTime
)

case class NewsItemSave (
  title: String,
  text: String,
  link: Option[Link],
  publishDate: DateTime
)

case class NewsCategory(id: String, name: String)
