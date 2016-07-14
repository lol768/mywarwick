package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import controllers.admin.NewsItemData
import models.news.{Audience, AudienceSize, NewsItemRender, NewsItemSave}
import play.api.db.Database
import services.dao.{AudienceDao, NewsDao, NewsCategoryDao}
import warwick.sso.Usercode

@ImplementedBy(classOf[AnormNewsService])
trait NewsService {
  def getAudience(newsId: String): Option[Audience]

  def getNewsByPublisher(publisherId: String, limit: Int, offset: Int = 0): Seq[NewsItemRender]

  def latestNews(user: Option[Usercode], limit: Int): Seq[NewsItemRender]
  def save(item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): String

  def updateNewsItem(id: String, item: NewsItemSave, audience: Audience, categoryIds: Seq[String])

  def countRecipients(newsIds: Seq[String]): Map[String, AudienceSize]

  def getNewsItem(id: String): Option[NewsItemRender]
}

class AnormNewsService @Inject()(
  db: Database,
  dao: NewsDao,
  audienceService: AudienceService,
  newsCategoryDao: NewsCategoryDao,
  audienceDao: AudienceDao,
  userInitialisationService: UserInitialisationService
) extends NewsService {

  override def getNewsByPublisher(publisherId: String, limit: Int, offset: Int): Seq[NewsItemRender] =
    db.withConnection { implicit c =>
      dao.allNews(publisherId, limit, offset)
    }

  override def latestNews(user: Option[Usercode], limit: Int): Seq[NewsItemRender] = {
    user.foreach(userInitialisationService.maybeInitialiseUser)

    db.withConnection { implicit c =>
      dao.latestNews(user, limit)
    }
  }

  // FIXME: Move audience-resolution and recipient-saving to scheduler
  // and just save audience components to db here
  override def save(item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): String =
    db.withTransaction { implicit c =>
      val recipients = audienceService.resolve(audience).get // FIXME Try.get throws
      val audienceId = audienceDao.saveAudience(audience)
      val id = dao.save(item, audienceId)
      dao.saveRecipients(id, item.publishDate, recipients)
      newsCategoryDao.saveNewsCategories(id, categoryIds)
      id
    }

  override def countRecipients(newsIds: Seq[String]): Map[String, AudienceSize] =
    db.withConnection { implicit c =>
      dao.countRecipients(newsIds)
    }

  override def updateNewsItem(id: String, item: NewsItemSave, audience: Audience, categoryIds: Seq[String]) =
    db.withTransaction { implicit c =>
      dao.updateNewsItem(id, item)
      newsCategoryDao.saveNewsCategories(id, categoryIds)

      val existingAudienceId = dao.getAudienceId(id)
      val existingAudience = existingAudienceId.map(audienceDao.getAudience)

      // If the audience has changed
      if (!existingAudience.contains(audience)) {
        // Swap the audience associated with the news item
        val audienceId = audienceDao.saveAudience(audience)
        dao.setAudienceId(id, audienceId)
        existingAudienceId.foreach(audienceDao.deleteAudience)

        // Set up the new recipients
        val recipients = audienceService.resolve(audience).get
        dao.deleteRecipients(id)
        dao.saveRecipients(id, item.publishDate, recipients)
      }
    }

  override def getAudience(newsId: String) =
    db.withConnection { implicit c =>
      dao.getAudienceId(newsId).map(audienceDao.getAudience)
    }

  override def getNewsItem(id: String): Option[NewsItemRender] =
    db.withConnection { implicit c =>
      dao.getNewsById(id)
    }
}