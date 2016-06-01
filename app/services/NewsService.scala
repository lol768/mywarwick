package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
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

}
