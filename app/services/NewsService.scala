package services

import java.sql.Connection
import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.news.{NewsItemRender, NewsItemSave}
import services.dao.NewsDao
import warwick.sso.Usercode

@ImplementedBy(classOf[AnormNewsService])
trait NewsService {
  def allNews(limit: Int = 100, offset: Int = 0)(implicit c: Connection): Seq[NewsItemRender]
  def latestNews(user: Usercode, limit: Int = 100)(implicit c: Connection): Seq[NewsItemRender]
  // TODO public news items -
  def save(item: NewsItemSave, recipients: Seq[Usercode])(implicit c: Connection): Unit
}

class AnormNewsService @Inject() (
  dao: NewsDao
) extends NewsService {
  override def allNews(limit: Int, offset: Int)(implicit c: Connection): Seq[NewsItemRender] =
    dao.allNews(limit, offset)

  override def latestNews(user: Usercode, limit: Int)(implicit c: Connection): Seq[NewsItemRender] =
    dao.latestNews(user, limit)

  override def save(item: NewsItemSave, recipients: Seq[Usercode])(implicit c: Connection): Unit = {
    val id = dao.save(item)
    dao.saveRecipients(id, item.publishDate, recipients)
  }

}
