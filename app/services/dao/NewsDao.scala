package services.dao

import java.sql.Connection
import java.util.UUID
import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.{AudienceSize, NewsCategory}
import models.news.{Link, NewsItemRender, NewsItemSave}
import org.joda.time.DateTime
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
  def allNews(publisherId: String, limit: Int = 100, offset: Int = 0)(implicit c: Connection): Seq[NewsItemRender]

  def latestNews(user: Option[Usercode], limit: Int = 100)(implicit c: Connection): Seq[NewsItemRender]

  /**
    * @param item the news item to save
    * @return the ID of the created item
    */
  def save(item: NewsItemSave, audienceId: String)(implicit c: Connection): String

  // TODO too many args? define an object?
  def setRecipients(newsId: String, recipients: Seq[Usercode])(implicit c: Connection): Unit

  def countRecipients(newsIds: Seq[String])(implicit c: Connection): Map[String, AudienceSize]

  def updateNewsItem(newsId: String, item: NewsItemSave)(implicit c: Connection): Int

  def getNewsById(id: String)(implicit c: Connection): Option[NewsItemRender] =
    getNewsByIds(Seq(id)).headOption

  def getNewsByIds(ids: Seq[String])(implicit c: Connection): Seq[NewsItemRender]

  def getAudienceId(newsId: String)(implicit c: Connection): Option[String]

  def setAudienceId(newsId: String, audienceId: String)(implicit c: Connection)

  def delete(newsId: String)(implicit c: Connection): Int

  def deleteRecipients(id: String)(implicit c: Connection): Unit
}

@Singleton
class AnormNewsDao @Inject()(dialect: DatabaseDialect) extends NewsDao {

  private def newId = UUID.randomUUID().toString

  def parseLink(href: Option[String]): Option[Uri] = href.flatMap { h =>
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
    imageId <- str("news_image_id").?
    ignoreCategories <- bool("ignore_categories")
    newsCategoryId <- str("news_category_id").?
    newsCategoryName <- str("news_category_name").?
    publisherId <- str("publisher_id")
  } yield {
    val link = for (text <- linkText; href <- parseLink(linkHref)) yield Link(text, href)
    val category = for (id <- newsCategoryId; name <- newsCategoryName) yield NewsCategory(id, name)

    NewsItemRender(id, title, text, link, publishDate, imageId, category.toSeq, ignoreCategories, publisherId)
  }

  override def allNews(publisherId: String, limit: Int, offset: Int)(implicit c: Connection): Seq[NewsItemRender] = {
    groupNewsItems(SQL(
      s"""
      SELECT
        n.*,
        NEWS_CATEGORY.ID AS NEWS_CATEGORY_ID,
        NEWS_CATEGORY.NAME AS NEWS_CATEGORY_NAME
      FROM NEWS_ITEM n
        LEFT OUTER JOIN NEWS_ITEM_CATEGORY c
          ON c.NEWS_ITEM_ID = n.ID
        LEFT OUTER JOIN NEWS_CATEGORY
          ON c.NEWS_CATEGORY_ID = NEWS_CATEGORY.ID
      WHERE n.PUBLISHER_ID = {publisherId}
      ORDER BY PUBLISH_DATE DESC
      ${dialect.limitOffset(limit, offset)}
      """)
      .on('publisherId -> publisherId)
      .as(newsParser.*))
  }

  override def latestNews(user: Option[Usercode], limit: Int)(implicit c: Connection): Seq[NewsItemRender] = {
    getNewsByIds(getNewsItemIdsForUser(user, limit))
  }

  def getNewsItemIdsForUser(user: Option[Usercode], limit: Int)(implicit c: Connection): Seq[String] = {
    SQL(
      s"""
        SELECT DISTINCT n.ID
        FROM NEWS_ITEM n
          JOIN NEWS_RECIPIENT r
            ON n.ID = r.NEWS_ITEM_ID
               AND (USERCODE = {user} OR USERCODE = '*')
               AND r.PUBLISH_DATE <= SYSDATE
          LEFT OUTER JOIN NEWS_ITEM_CATEGORY c
            ON c.NEWS_ITEM_ID = n.ID
        WHERE n.IGNORE_CATEGORIES = 1 OR c.NEWS_CATEGORY_ID IN
                                         (SELECT NEWS_CATEGORY_ID
                                          FROM USER_NEWS_CATEGORY
                                          WHERE USERCODE = {user})
        ${dialect.limitOffset(limit)}
       """)
      .on('user -> user.map(_.string).getOrElse("*"))
      .as(scalar[String].*)
  }

  /**
    * Save a news item with a specific set of recipients.
    */
  override def save(item: NewsItemSave, audienceId: String)(implicit c: Connection): String = {
    import item._
    val id = newId
    val linkText = link.map(_.text).orNull
    val linkHref = link.map(_.href.toString).orNull
    SQL"""
    INSERT INTO NEWS_ITEM (id, title, text, link_text, link_href, news_image_id, created_at, publish_date, audience_id, ignore_categories, publisher_id, created_by)
    VALUES ($id, $title, $text, $linkText, $linkHref, $imageId, SYSDATE, $publishDate, $audienceId, $ignoreCategories, $publisherId, ${usercode.string})
    """.executeUpdate()
    id
  }

  override def setRecipients(newsId: String, recipients: Seq[Usercode])(implicit c: Connection): Unit = {
    deleteRecipients(newsId) // delete existing News item recipients
    val publishDate = SQL"SELECT publish_date FROM news_item WHERE id=$newsId".as(get[DateTime]("publish_date").single)
    recipients.foreach { usercode =>
      SQL"""
        INSERT INTO NEWS_RECIPIENT (news_item_id, usercode, publish_date)
        VALUES ($newsId, ${usercode.string}, $publishDate)
      """.executeUpdate()
    }
  }

  override def delete(newsId: String)(implicit c: Connection) =
    SQL"DELETE FROM news_item WHERE id=$newsId".executeUpdate()

  /**
    * Deletes all the recipients of a news item.
    */
  override def deleteRecipients(id: String)(implicit c: Connection) = {
    SQL"DELETE FROM NEWS_RECIPIENT WHERE news_item_id = $id".executeUpdate()
  }

  override def getAudienceId(newsId: String)(implicit c: Connection) = {
    SQL"SELECT AUDIENCE_ID FROM NEWS_ITEM WHERE ID = $newsId"
      .executeQuery()
      .as(scalar[String].singleOpt)
  }

  override def setAudienceId(newsId: String, audienceId: String)(implicit c: Connection) = {
    SQL"UPDATE NEWS_ITEM SET AUDIENCE_ID = $audienceId WHERE ID = $newsId"
      .execute()
  }

  private val countParser = (str("id") ~ int("c")).map(flatten)

  override def countRecipients(newsIds: Seq[String])(implicit c: Connection): Map[String, AudienceSize] = {
    import AudienceSize._
    if (newsIds.isEmpty) {
      Map.empty
    } else {
      val publicNews =
        SQL"""
        SELECT NEWS_ITEM_ID ID FROM NEWS_RECIPIENT WHERE USERCODE = '*'
        AND NEWS_ITEM_ID IN ($newsIds)
      """.as(str("id").*).map(_ -> Public).toMap

      val audienceNews =
        SQL"""
           SELECT
             nr.NEWS_ITEM_ID ID,
             COUNT(*)        C
           FROM NEWS_RECIPIENT nr
             JOIN NEWS_ITEM ni ON ni.ID = nr.NEWS_ITEM_ID
           WHERE nr.USERCODE != '*' AND nr.NEWS_ITEM_ID IN ($newsIds)
                 AND (ni.IGNORE_CATEGORIES = 1 OR nr.USERCODE IN
                                                  (SELECT unc.USERCODE
                                                   FROM USER_NEWS_CATEGORY unc
                                                     JOIN NEWS_ITEM_CATEGORY nic
                                                       ON unc.NEWS_CATEGORY_ID = nic.NEWS_CATEGORY_ID
                                                          AND nr.NEWS_ITEM_ID = nic.NEWS_ITEM_ID))
           GROUP BY nr.NEWS_ITEM_ID
      """.as(countParser.*).toMap.mapValues(Finite)

      publicNews ++ audienceNews
    }
  }

  override def updateNewsItem(newsId: String, item: NewsItemSave)(implicit c: Connection): Int = {
    import item._
    val linkText = link.map(_.text).orNull
    val linkHref = link.map(_.href.toString).orNull
    SQL"""
      UPDATE NEWS_ITEM SET title=$title, text=$text, link_text=$linkText,
      link_href=$linkHref, publish_date=$publishDate, news_image_id=$imageId,
      updated_at=SYSDATE, updated_by=${usercode.string}
      WHERE id=$newsId
      """.executeUpdate()
  }

  override def getNewsByIds(ids: Seq[String])(implicit c: Connection): Seq[NewsItemRender] = {
    groupNewsItems(ids.grouped(1000).flatMap { ids =>
      SQL"""
      SELECT
        n.*,
        NEWS_CATEGORY.ID AS NEWS_CATEGORY_ID,
        NEWS_CATEGORY.NAME AS NEWS_CATEGORY_NAME
      FROM NEWS_ITEM n
        LEFT OUTER JOIN NEWS_ITEM_CATEGORY c
          ON c.NEWS_ITEM_ID = n.ID
        LEFT OUTER JOIN NEWS_CATEGORY
          ON c.NEWS_CATEGORY_ID = NEWS_CATEGORY.ID
      WHERE n.ID IN ($ids)
      """.as(newsParser.*)
    }.toSeq)
  }

  val mostRecentFirst: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

  def groupNewsItems(items: Seq[NewsItemRender]): Seq[NewsItemRender] = {
    items
      .groupBy(_.id)
      .mapValues(items => items.head.copy(categories = items.flatMap(_.categories)))
      .values
      .toSeq
      .sortBy(_.publishDate)(mostRecentFirst)
  }

}
