package models.news

import controllers.publish.NewsItemData
import models.{DateFormats, NewsCategory}
import org.joda.time.DateTime
import play.api.libs.json._
import uk.ac.warwick.util.web.Uri
import warwick.sso.{User, Usercode}

case class Link(text: String, href: Uri)
object Link {
  implicit val jsonWriter = new Writes[Link] {
    override def writes(o: Link): JsValue = Json.obj(
      "text" -> o.text,
      "href" -> o.href.toString
    )
  }

  // Never used; required only to create format for NewsItemRender, required for
  // news feed endpoint to use API.Success
  implicit val jsonReader = new Reads[Link] {
    override def reads(json: JsValue): JsResult[Link] = ???
  }
}

sealed trait NewsItemRenderFields {
  val id: String
  val title: String
  val text: String // This is plain newline-separated text
  val link: Option[Link]
  val publishDate: DateTime
  val imageId: Option[String]
  val categories: Seq[NewsCategory]
  val ignoreCategories: Boolean
  val publisherId: String
}

/**
  * The information about a news item that we fetch out of the database
  * in order to render it.
  */
case class NewsItemRender (
  id: String,
  title: String,
  text: String, // This is plain newline-separated text
  link: Option[Link],
  publishDate: DateTime,
  imageId: Option[String],
  categories: Seq[NewsCategory],
  ignoreCategories: Boolean,
  publisherId: String
) extends NewsItemRenderFields {
  def toData: NewsItemData = NewsItemData(
    title,
    text,
    link.map(_.text),
    link.map(_.href.toString),
    publishDateSet = true,
    publishDate.toLocalDateTime,
    imageId,
    ignoreCategories = ignoreCategories
  )
}

object NewsItemRender {
  implicit private val dateWriter = DateFormats.isoDateWrites
  implicit val format = Json.format[NewsItemRender]
}

sealed trait NewsItemAuditFields[U] {
  val id: String
  val created: DateTime
  val createdBy: Option[U]
  val updated: Option[DateTime]
  val updatedBy: Option[U]
}

case class NewsItemAudit[U] (
  id: String,
  created: DateTime,
  createdBy: Option[U],
  updated: Option[DateTime],
  updatedBy: Option[U]
) extends NewsItemAuditFields[U]

object NewsItemAudit {
  type Light = NewsItemAudit[Usercode]
  type Heavy = NewsItemAudit[User]
}

case class NewsItemRenderWithAudit (
  id: String,
  title: String,
  text: String, // This is plain newline-separated text
  link: Option[Link],
  publishDate: DateTime,
  imageId: Option[String],
  categories: Seq[NewsCategory],
  ignoreCategories: Boolean,
  publisherId: String,
  created: DateTime,
  createdBy: Option[User],
  updated: Option[DateTime],
  updatedBy: Option[User]
) extends NewsItemRenderFields with NewsItemAuditFields[User]

object NewsItemRenderWithAudit {
  def applyWithAudit(news: NewsItemRender, audit: NewsItemAudit[User]) = NewsItemRenderWithAudit(
    id = news.id,
    title = news.title,
    text = news.text,
    link = news.link,
    publishDate = news.publishDate,
    imageId = news.imageId,
    categories = news.categories,
    ignoreCategories = news.ignoreCategories,
    publisherId = news.publisherId,
    created = audit.created,
    createdBy = audit.createdBy,
    updated = audit.updated,
    updatedBy = audit.updatedBy
  )
}

/**
  * The data we need about a new news item to save it as a row.
  */
case class NewsItemSave (
  usercode: Usercode,
  publisherId: String,
  title: String,
  text: String,
  link: Option[Link],
  publishDate: DateTime,
  imageId: Option[String],
  ignoreCategories: Boolean = false
  // TODO publisher
)
