package services.dao

import java.sql.Connection
import java.util.UUID
import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.Audience.DepartmentSubset
import models.news.{Link, NewsItemAudit, NewsItemRender, NewsItemSave}
import models.{AudienceSize, NewsCategory}
import org.joda.time.DateTime
import system.DatabaseDialect
import uk.ac.warwick.util.web.Uri
import uk.ac.warwick.util.web.Uri.UriException
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.{GroupName, Usercode}

@ImplementedBy(classOf[AnormNewsDao])
trait NewsDao {

  /**
    * All the news, all the time. This is probably only useful for sysadmins and maybe Comms.
    * Everyone else will be getting a filtered view.
    */
  def allNews(publisherId: String, limit: Int = 100, offset: Int = 0)(implicit c: Connection): Seq[NewsItemRender]

  def latestNews(user: Option[Usercode], limit: Int = 100, offset: Int = 0)(implicit c: Connection): Seq[NewsItemRender]

  /**
    * @param item the news item to save
    * @return the ID of the created item
    */
  def save(item: NewsItemSave, audienceId: String, audienceSize: AudienceSize)(implicit c: Connection): String

  def setRecipients(newsId: String, recipients: Set[Usercode])(implicit c: Connection): Unit

  def updateNewsItem(newsId: String, item: NewsItemSave, audienceSize: AudienceSize)(implicit c: Connection): Int

  def getNewsById(id: String)(implicit c: Connection): Option[NewsItemRender] =
    getNewsByIds(Seq(id)).headOption

  def getNewsByIds(ids: Seq[String])(implicit c: Connection): Seq[NewsItemRender]

  def getNewsAuditByIds(ids: Seq[String])(implicit c: Connection): Seq[NewsItemAudit.Light]

  def getAudienceId(newsId: String)(implicit c: Connection): Option[String]

  def setAudienceId(newsId: String, audienceId: String)(implicit c: Connection)

  def delete(newsId: String)(implicit c: Connection): Int

  def deleteRecipients(id: String)(implicit c: Connection): Unit

  def getNewsItemsMatchingAudience(webGroup: Option[GroupName], departmentCode: Option[String], departmentSubset: Option[DepartmentSubset], publisherId: Option[String], limit: Int)(implicit c: Connection): Seq[String]

  def updateAudienceCount(newsId: String, audienceSize: AudienceSize)(implicit c: Connection): Int
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

  private val newsParser = for {
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

  private val newsAuditParser = for {
    id <- str("id")
    created <- get[DateTime]("created_at")
    createdBy <- str("created_by")
    updated <- get[DateTime]("updated_at").?
    updatedBy <- str("updated_by").?
    audienceSize <- get[Option[Int]]("audience_size")
  } yield {
    NewsItemAudit(id, created, Some(Usercode(createdBy)), updated, updatedBy.map(Usercode), AudienceSize.fromOption(audienceSize))
  }

  override def allNews(publisherId: String, limit: Int, offset: Int)(implicit c: Connection): Seq[NewsItemRender] = {
    groupNewsItems(SQL(dialect.limitOffset(limit, offset)(
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
      """))
      .on('publisherId -> publisherId)
      .as(newsParser.*))
  }

  override def latestNews(user: Option[Usercode], limit: Int, offset: Int)(implicit c: Connection): Seq[NewsItemRender] = {
    getNewsByIds(getNewsItemIdsForUser(user, limit, offset))
  }

  def getNewsItemIdsForUser(user: Option[Usercode], limit: Int, offset: Int)(implicit c: Connection): Seq[String] = {
    SQL(dialect.limitOffset(limit, offset)(
      s"""
        SELECT DISTINCT n.ID, n.PUBLISH_DATE
        FROM NEWS_ITEM n
          JOIN NEWS_RECIPIENT r
            ON n.ID = r.NEWS_ITEM_ID
               AND (USERCODE = {user} OR USERCODE = '*')
               AND r.PUBLISH_DATE <= SYSDATE
          LEFT OUTER JOIN NEWS_ITEM_CATEGORY c
            ON c.NEWS_ITEM_ID = n.ID
        WHERE n.IGNORE_CATEGORIES = 1
          OR c.NEWS_CATEGORY_ID NOT IN (
            SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY
            WHERE USERCODE = {user} AND SELECTED = 0
          ) OR {user} = '*'
        ORDER BY n.PUBLISH_DATE DESC
       """))
      .on(
        'user -> user.map(_.string).getOrElse("*")
      )
      .as(scalar[String].*)
  }

  /**
    * Save a news item with a specific set of recipients.
    */
  override def save(item: NewsItemSave, audienceId: String, audienceSize: AudienceSize)(implicit c: Connection): String = {
    import item._
    val id = newId
    val linkText = link.map(_.text).orNull
    val linkHref = link.map(_.href.toString).orNull
    SQL"""
    INSERT INTO NEWS_ITEM (id, title, text, link_text, link_href, news_image_id, created_at, publish_date, audience_id, ignore_categories, publisher_id, created_by, audience_size)
    VALUES ($id, $title, $text, $linkText, $linkHref, $imageId, SYSDATE, $publishDate, $audienceId, $ignoreCategories, $publisherId, ${usercode.string}, ${audienceSize.toOption})
    """.executeUpdate()
    id
  }

  override def setRecipients(newsId: String, recipients: Set[Usercode])(implicit c: Connection): Unit = {
    deleteRecipients(newsId) // delete existing News item recipients
    val publishDate = SQL"SELECT publish_date FROM news_item WHERE id=$newsId".as(get[DateTime]("publish_date").single)
    recipients.foreach { usercode =>
      SQL"""
        INSERT INTO NEWS_RECIPIENT (news_item_id, usercode, publish_date)
        VALUES ($newsId, ${usercode.string}, $publishDate)
      """.executeUpdate()
    }
  }

  override def delete(newsId: String)(implicit c: Connection): Int =
    SQL"DELETE FROM news_item WHERE id=$newsId".executeUpdate()

  /**
    * Deletes all the recipients of a news item.
    */
  override def deleteRecipients(id: String)(implicit c: Connection): Unit = {
    SQL"DELETE FROM NEWS_RECIPIENT WHERE news_item_id = $id".executeUpdate()
  }

  override def getAudienceId(newsId: String)(implicit c: Connection): Option[String] = {
    SQL"SELECT AUDIENCE_ID FROM NEWS_ITEM WHERE ID = $newsId"
      .executeQuery()
      .as(scalar[String].singleOpt)
  }

  override def setAudienceId(newsId: String, audienceId: String)(implicit c: Connection): Unit = {
    SQL"UPDATE NEWS_ITEM SET AUDIENCE_ID = $audienceId WHERE ID = $newsId"
      .execute()
  }

  override def updateNewsItem(newsId: String, item: NewsItemSave, audienceSize: AudienceSize)(implicit c: Connection): Int = {
    import item._
    val linkText = link.map(_.text).orNull
    val linkHref = link.map(_.href.toString).orNull
    SQL"""
      UPDATE NEWS_ITEM SET title=$title, text=$text, link_text=$linkText,
      link_href=$linkHref, publish_date=$publishDate, news_image_id=$imageId,
      updated_at=SYSDATE, updated_by=${usercode.string}, audience_size=${audienceSize.toOption}
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

  override def getNewsAuditByIds(ids: Seq[String])(implicit c: Connection): Seq[NewsItemAudit.Light] = {
    ids.grouped(1000).flatMap { ids =>
      SQL"""SELECT n.* FROM NEWS_ITEM n WHERE n.ID IN ($ids)""".as(newsAuditParser.*)
    }.toSeq
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

  def getNewsItemsMatchingAudience(webGroup: Option[GroupName], departmentCode: Option[String], departmentSubset: Option[DepartmentSubset], publisherId: Option[String], limit: Int)(implicit c: Connection): List[String] = {
    if (webGroup.isEmpty && departmentCode.isEmpty && departmentSubset.isEmpty && publisherId.isEmpty) {
      Nil
    } else {
      var or = Seq.empty[String]

      if (departmentSubset.nonEmpty || departmentCode.nonEmpty) {
        or :+=
          s"""(AUDIENCE_COMPONENT.DEPT_CODE = {departmentCode}
            ${departmentSubset.map(_ => " AND AUDIENCE_COMPONENT.NAME = {departmentSubset}").getOrElse("")})"""
      }

      if (webGroup.nonEmpty) {
        or :+= "(AUDIENCE_COMPONENT.NAME = 'WebGroup' AND AUDIENCE_COMPONENT.VALUE = {webGroup})"
      }

      if (publisherId.nonEmpty) {
        or :+= "(NEWS_ITEM.PUBLISHER_ID = {publisherId})"
      }

      val query = dialect.limitOffset(limit) {
        s"""SELECT DISTINCT NEWS_ITEM.ID, NEWS_ITEM.PUBLISH_DATE FROM NEWS_ITEM
          JOIN AUDIENCE_COMPONENT ON NEWS_ITEM.AUDIENCE_ID = AUDIENCE_COMPONENT.AUDIENCE_ID
          WHERE ${or.mkString(" OR ")} ORDER BY NEWS_ITEM.PUBLISH_DATE DESC"""
      }

      SQL(query)
        .on(
          'webGroup -> webGroup.map(_.string).orNull,
          'departmentCode -> departmentCode.orNull,
          'departmentSubset -> departmentSubset.map(_.entryName).orNull,
          'publisherId -> publisherId.orNull
        )
        .executeQuery()
        .as(scalar[String].*)
    }
  }

  override def updateAudienceCount(newsId: String, audienceSize: AudienceSize)(implicit c: Connection): Int = {
    SQL"""
      UPDATE NEWS_ITEM SET audience_size=${audienceSize.toOption}
      WHERE id=$newsId
    """.executeUpdate()
  }


}
