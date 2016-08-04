package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.news._
import play.api.db.Database
import services.dao.{AudienceDao, NewsCategoryDao, NewsDao}
import warwick.sso.Usercode

@ImplementedBy(classOf[AnormNewsService])
trait NewsService {
  def setPublished(newsItemId: String): Unit

  def getNewsItemsToPublishNow(): Seq[NewsItemIdAndAudienceId]

  def getAudience(newsId: String): Option[Audience]

  def getNewsByPublisher(publisherId: String, limit: Int, offset: Int = 0): Seq[NewsItemRender]

  def latestNews(user: Option[Usercode], limit: Int): Seq[NewsItemRender]

  def save(item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): String

  def update(id: String, item: NewsItemSave, audience: Audience, categoryIds: Seq[String])

  def countRecipients(newsIds: Seq[String]): Map[String, AudienceSize]

  def getNewsItem(id: String): Option[NewsItemRender]

  def setRecipients(newsItemId: String, recipients: Seq[Usercode]): Unit
}

class AnormNewsService @Inject()(
  db: Database,
  dao: NewsDao,
  audienceService: AudienceService,
  newsCategoryDao: NewsCategoryDao,
  audienceDao: AudienceDao,
  userInitialisationService: UserInitialisationService,
  scheduler: ScheduleJobService
) extends NewsService {

  override def setPublished(newsItemId: String): Unit =
    db.withConnection(implicit c => dao.setPublished(newsItemId))

  override def getNewsItemsToPublishNow(): Seq[NewsItemIdAndAudienceId] =
    db.withConnection(implicit c => dao.getNewsItemsToPublishNow())

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

  override def save(item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): String =
    db.withTransaction { implicit c =>
      val audienceId = audienceDao.saveAudience(audience)
      val id = dao.save(item, audienceId)
      newsCategoryDao.saveNewsCategories(id, categoryIds)
      id
    }

  override def countRecipients(newsIds: Seq[String]): Map[String, AudienceSize] =
    db.withConnection { implicit c =>
      dao.countRecipients(newsIds)
    }

  override def update(id: String, item: NewsItemSave, audience: Audience, categoryIds: Seq[String]) =
    db.withTransaction { implicit c =>
      dao.updateNewsItem(id, item)
      newsCategoryDao.deleteNewsCategories(id)
      newsCategoryDao.saveNewsCategories(id, categoryIds)

      val existingAudienceId = dao.getAudienceId(id)
      val existingAudience = existingAudienceId.map(audienceDao.getAudience)

      // If the audience has changed
      if (!existingAudience.contains(audience)) {
        // Swap the audience associated with the news item
        val audienceId = audienceDao.saveAudience(audience)
        dao.setAudienceId(id, audienceId)
        existingAudienceId.foreach(audienceDao.deleteAudience)
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

  /**
    * Deletes all the recipients of a news item and replaces them with recipients param
    * @param newsItemId
    * @param recipients
    */
  override def setRecipients(newsItemId: String, recipients: Seq[Usercode]) =
    db.withTransaction(implicit c => dao.setRecipients(newsItemId, recipients))
}