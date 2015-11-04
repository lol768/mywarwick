package services

import models.{Feed, FeedItem, NewsSource}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.http.Status.OK
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FeedService {

  implicit val feedItemReads: Reads[FeedItem] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "url" \ "href").read[String] and
    (JsPath \ "content").read[String] and
    (JsPath \ "publicationDate").read[DateTime]
        )(FeedItem.apply _)

  implicit val feedReads: Reads[Feed] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "items").read[Seq[FeedItem]]
    )(Feed.apply _)

  val SitebuilderNewsJsonUrl = "http://www2.warwick.ac.uk/sitebuilder2/api/rss/news.json"

  def fetch(source: NewsSource): Future[Feed] = {
    WS.url(SitebuilderNewsJsonUrl).withQueryString("page" -> source.url)
      .get()
      .filter(_.status == OK)
      .map(response => Json.parse(response.body).as[Feed])
  }

}
