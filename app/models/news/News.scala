package models.news

import models.DateFormats
import org.joda.time.DateTime
import play.api.libs.json._
import uk.ac.warwick.util.web.Uri

case class Link(text: String, href: Uri)
object Link {
  implicit val jsonWriter = new Writes[Link] {
    override def writes(o: Link): JsValue = Json.obj(
      "text" -> o.text,
      "href" -> o.href.toString
    )
  }
}

/**
  * The information about a news item that we fetch out of the database
  * in order to render it.
  */
case class NewsItemRender (
  id: String,
  title: String,
  text: String, // TODO what is this, HTML? Plaintext? Some other "safe" markup?
  link: Option[Link],
  publishDate: DateTime,
  imageId: Option[String]
  // TODO Add category info
  // TODO Add publisher info
)

object NewsItemRender {
  implicit private val dateWriter = DateFormats.isoDateWrites
  implicit val jsonWriter = Json.writes[NewsItemRender]
}

/**
  * The data we need about a new news item to save it as a row.
  */
case class NewsItemSave (
  title: String,
  text: String,
  link: Option[Link],
  publishDate: DateTime,
  imageId: Option[String]
  // TODO publisher, category/ies
)

case class NewsCategory(id: String, name: String)
