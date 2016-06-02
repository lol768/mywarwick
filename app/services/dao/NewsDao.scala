package services.dao

import java.sql.Connection
import java.util.UUID
import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.news.{AudienceSize, Link, NewsItemRender, NewsItemSave}
import org.joda.time.DateTime
import play.api.db.Database
import system.DatabaseDialect
import uk.ac.warwick.util.web.Uri
import uk.ac.warwick.util.web.Uri.UriException
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[AnormNewsDao])
trait NewsDao {

  /**
    * All the news, all the time. This is probably only useful for sysadmins and maybe Comms.
    * Everyone else will be getting a filtered view.
    */
  def allNews(limit: Int = 100, offset: Int = 0)(implicit c: Connection): Seq[NewsItemRender]

  def latestNews(user: Option[Usercode], limit: Int = 100)(implicit c: Connection): Seq[NewsItemRender]

  /**
    * @param item the news item to save
    * @return the ID of the created item
    */
  // TODO public news items
  def save(item: NewsItemSave)(implicit c: Connection): String

  // TODO too many args? define an object?
  def saveRecipients(newsId: String, publishDate: DateTime, recipients: Seq[Usercode])(implicit c: Connection): Unit

  def countRecipients(newsIds: Seq[String])(implicit c: Connection): Map[String, AudienceSize]
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

  def latestNews(user: Option[Usercode], limit: Int = 100)(implicit c: Connection): Seq[NewsItemRender] = {
    SQL(s"""
      SELECT n.* FROM news_item n
      JOIN news_recipient r ON n.id = r.news_item_id
      AND (usercode = {user} OR usercode = '*')
      AND r.publish_date < SYSDATE
      ORDER BY r.publish_date DESC
      ${dialect.limitOffset(limit)}
      """).on('user -> user.map(_.string).getOrElse("*")).as(newsParser.*)
  }

  /**
    * Save a news item with a specific set of recipients.
    */
  def save(item: NewsItemSave)(implicit c: Connection): String = {
    import item._
    val id = newId
    val linkText = link.map(_.text).orNull
    val linkHref = link.map(_.href.toString).orNull
    SQL"""
    INSERT INTO NEWS_ITEM (id, title, text, link_text, link_href, created_at, publish_date)
    VALUES (${id}, ${title}, ${text}, ${linkText}, ${linkHref}, SYSDATE, ${publishDate})
    """.executeUpdate()
    id
  }

  def saveRecipients(newsId: String, publishDate: DateTime, recipients: Seq[Usercode])(implicit c: Connection): Unit = {
    // TODO perhaps we shouldn't insert these in sync, as audiences can potentially be 1000s users.
    recipients.foreach { usercode =>
      SQL"""
        INSERT INTO NEWS_RECIPIENT (news_item_id, usercode, publish_date)
        VALUES (${newsId}, ${usercode.string}, ${publishDate})
      """.executeUpdate()
    }
  }

  /**
    * Deletes all the recipients of a news item.
    */
  def deleteRecipients(id: String)(implicit c: Connection) = {
    SQL"DELETE FROM NEWS_RECIPIENT WHERE news_item_id = ${id}".executeUpdate()
  }

  private val countParser = (str("id") ~ int("c")).map(flatten)

  def countRecipients(newsIds: Seq[String])(implicit c: Connection): Map[String, AudienceSize] = {
    import AudienceSize._

    val publicNews =
      SQL"SELECT NEWS_ITEM_ID ID FROM NEWS_RECIPIENT WHERE USERCODE = '*'"
        .as(str("id").*).map(_ -> Public).toMap

    val audienceNews =
      SQL"""
      SELECT NEWS_ITEM_ID ID, COUNT(*) C FROM NEWS_RECIPIENT
      WHERE NEWS_ITEM_ID IN ($newsIds) AND USERCODE != '*'
      GROUP BY NEWS_ITEM_ID
    """.as(countParser.*).toMap.mapValues(Finite)

    publicNews ++ audienceNews
  }

}
