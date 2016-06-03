package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import controllers.admin.{NewsItemData, NewsItemUpdate}
import models.news.{Audience, NewsItemRender, NewsItemSave}
import play.api.db.Database
import services.dao.NewsDao
import warwick.sso.Usercode

@ImplementedBy(classOf[AnormNewsService])
trait NewsService {
  def allNews(limit: Int = 100, offset: Int = 0): Seq[NewsItemRender]
  def latestNews(user: Usercode, limit: Int = 100): Seq[NewsItemRender]
  // TODO public news items
  def save(item: NewsItemSave, audience: Audience): Unit

  def countRecipients(newsIds: Seq[String]): Map[String, Int]
  def updateNewsItem(id: String, item: NewsItemData): Int
  def get(id: String): Option[NewsItemRender]
  def unpublish(id: String): Int
}

class AnormNewsService @Inject() (
  db: Database,
  dao: NewsDao,
  audienceService: AudienceService
) extends NewsService {

  override def allNews(limit: Int, offset: Int): Seq[NewsItemRender] =
    db.withConnection { implicit c =>
      dao.allNews(limit, offset)
    }

  override def latestNews(user: Usercode, limit: Int): Seq[NewsItemRender] =
    db.withConnection { implicit c =>
      dao.latestNews(user, limit)
    }

  override def save(item: NewsItemSave, audience: Audience): Unit =
    db.withConnection { implicit c =>
      val recipients = audienceService.resolve(audience).get // FIXME Try.get throws
      val id = dao.save(item)
      dao.saveRecipients(id, item.publishDate, recipients)
    }

  override def countRecipients(newsIds: Seq[String]): Map[String, Int] =
    db.withConnection { implicit c =>
      dao.countRecipients(newsIds)
    }

  override def updateNewsItem(id: String, item: NewsItemData): Int =
    db.withConnection { implicit c =>
      dao.updateNewsItem(id, item.toSave)
    }

  override def get(id: String): Option[NewsItemRender] =
    db.withConnection { implicit c =>
      dao.getNewsById(id)
    }

  override def unpublish(id: String): Int =
    db.withConnection {implicit c =>
      dao.removeRecipients(id)
  }
}
