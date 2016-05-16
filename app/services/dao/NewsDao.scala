package services.dao

import java.sql.Connection
import java.util.UUID
import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.news.{Link, NewsItemRender, NewsItemSave}
import org.joda.time.DateTime
import play.api.db.Database
import system.DatabaseDialect
import uk.ac.warwick.util.web.Uri
import uk.ac.warwick.util.web.Uri.UriException
import warwick.sso.Usercode
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[AnormNewsDao])
trait NewsDao {
  /**
    * All the news, all the time. This is probably only useful for sysadmins and maybe Comms.
    * Everyone else will be getting a filtered view.
    */
  def allNews(limit: Int = 100, offset: Int = 0)(implicit c: Connection): Seq[NewsItemRender]

  def latestNews(user: Usercode, limit: Int = 100)(implicit c: Connection): Seq[NewsItemRender]
  def save(item: NewsItemSave, recipients: Seq[Usercode])(implicit c: Connection): Unit
}

@Singleton
class AnormNewsDao @Inject()(db: Database, dialect: DatabaseDialect) extends NewsDao {

  private def newId = UUID.randomUUID().toString

  def parseLink(href: Option[String]) : Option[Uri] = href.flatMap { h =>
    try {
      Option(Uri.parse(h))
    } catch {
      case e: UriException => None
    }
  }

  val newsParser = for {
    id <- str("id")
    title <- str("title")
    text <- str("text")
    linkText <- str("link_text").?
    linkHref <- str("link_href").?
    publishDate <- get[DateTime]("publish_date")
  } yield {
    val link = for { t <- linkText; h <- parseLink(linkHref) } yield Link(t, h)
    NewsItemRender(id, title, text, link, publishDate)
  }

  def allNews(limit: Int = 100, offset: Int = 0)(implicit c: Connection): Seq[NewsItemRender] = {
    SQL(s"""
      SELECT n.* FROM news_item n
      ORDER BY publish_date DESC
      ${dialect.limitOffset(limit, offset)}
      """).as(newsParser.*)
  }

  def latestNews(user: Usercode, limit: Int = 100)(implicit c: Connection): Seq[NewsItemRender] = {
    SQL(s"""
      SELECT n.* FROM news_item n
      JOIN news_recipient r ON n.id = r.news_item_id
      AND usercode = {user}
      AND r.publish_date < SYSDATE
      ORDER BY r.publish_date DESC
      ${dialect.limitOffset(limit)}
      """).on('user -> user.string).as(newsParser.*)
  }

  /**
    * Save a news item with a specific set of recipients.
    */
  def save(item: NewsItemSave, recipients: Seq[Usercode])(implicit c: Connection): Unit = {
    import item._
    val id = newId
    val linkText = link.map(_.text).orNull
    val linkHref = link.map(_.href.toString).orNull
    SQL"""
    INSERT INTO NEWS_ITEM (id, title, text, link_text, link_href, created_at, publish_date)
    VALUES (${id}, ${title}, ${text}, ${linkText}, ${linkHref}, SYSDATE, ${publishDate})
    """.executeUpdate()

    recipients.foreach { usercode =>
      SQL"""
        INSERT INTO NEWS_RECIPIENT (news_item_id, usercode, publish_date)
        VALUES (${id}, ${usercode.string}, ${publishDate})
      """.executeUpdate()
    }
  }

  def unpublish(id: String)(implicit c: Connection) = {
    SQL"DELETE FROM NEWS_RECIPIENT WHERE news_item_id = ${id}".executeUpdate()
  }


}
