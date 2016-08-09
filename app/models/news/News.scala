package models.news

import controllers.admin.NewsItemData
import models.{DateFormats, NewsCategory}
import oracle.net.aso.h
import org.joda.time.DateTime
import play.api.libs.json._
import uk.ac.warwick.util.web.Uri
import warwick.sso.Usercode

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
) {
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
