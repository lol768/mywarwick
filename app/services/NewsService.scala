package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.news.{NewsItemRender, NewsItemSave}
import models.{Audience, AudienceSize}
import org.joda.time.DateTime
import org.quartz.JobBuilder.newJob
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import org.quartz._
import play.api.db.Database
import services.dao.{AudienceDao, NewsCategoryDao, NewsDao, NewsImageDao}
import services.job.PublishNewsItemJob
import system.ThreadPools.web
import warwick.sso.Usercode

import scala.concurrent.Future

@ImplementedBy(classOf[AnormNewsService])
trait NewsService {
  def getAudience(newsId: String): Option[Audience]

  def getNewsByPublisher(publisherId: String, limit: Int, offset: Int = 0): Seq[NewsItemRender]

  def latestNews(user: Option[Usercode], limit: Int, offset: Int = 0): Seq[NewsItemRender]

  def save(item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): String

  // Updates a news item.  This happens in two parts for performance reasons.
  //
  // When the method returns:
  //   - the news item has been updated
  //   - the news categories have been updated
  //   - the new audience has been saved and associated with the news item
  //
  // When the returned Future completes:
  //   - any existing recipients have been deleted
  //   - the job to publish the news item has been scheduled
  def update(id: String, item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): Future[Unit]

  def countRecipients(newsIds: Seq[String]): Map[String, AudienceSize]

  def getNewsItem(id: String): Option[NewsItemRender]

  def setRecipients(newsItemId: String, recipients: Seq[Usercode]): Unit

  def delete(newsItemId: String): Unit
}

class AnormNewsService @Inject()(
  db: Database,
  dao: NewsDao,
  audienceService: AudienceService,
  newsCategoryDao: NewsCategoryDao,
  newsImageDao: NewsImageDao,
  audienceDao: AudienceDao,
  userInitialisationService: UserInitialisationService,
  scheduler: SchedulerService
) extends NewsService {

  override def delete(newsId: String) = db.withTransaction { implicit c =>
    newsImageDao.deleteForNewsItemId(newsId)
    newsCategoryDao.deleteNewsCategories(newsId)
    dao.deleteRecipients(newsId)
    dao.delete(newsId)
  }

  override def getNewsByPublisher(publisherId: String, limit: Int, offset: Int): Seq[NewsItemRender] =
    db.withConnection { implicit c =>
      dao.allNews(publisherId, limit, offset)
    }

  override def latestNews(user: Option[Usercode], limit: Int, offset: Int): Seq[NewsItemRender] = {
    user.foreach(userInitialisationService.maybeInitialiseUser)

    db.withConnection { implicit c =>
      dao.latestNews(user, limit, offset)
    }
  }

  private def schedulePublishNewsItem(newsId: String, audienceId: String, publishDate: DateTime): Unit = {
    val key = new JobKey(newsId, PublishNewsItemJob.name)

    // Delete any existing job that would publish the same news item
    scheduler.deleteJob(key)

    val job = newJob(classOf[PublishNewsItemJob])
      .withIdentity(key)
      .usingJobData("newsItemId", newsId)
      .usingJobData("audienceId", audienceId)
      .build()

    if (publishDate.isAfterNow) {
      val trigger = newTrigger()
        .startAt(publishDate.toDate)
        .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
        .build()

      scheduler.scheduleJob(job, trigger)
    } else {
      scheduler.triggerJobNow(job)
    }
  }

  override def save(item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): String =
    db.withTransaction { implicit c =>
      val audienceId = audienceDao.saveAudience(audience)
      val id = dao.save(item, audienceId)
      newsCategoryDao.saveNewsCategories(id, categoryIds)
      schedulePublishNewsItem(id, audienceId, item.publishDate)
      id
    }

  override def countRecipients(newsIds: Seq[String]): Map[String, AudienceSize] =
    db.withConnection { implicit c =>
      dao.countRecipients(newsIds)
    }

  override def update(id: String, item: NewsItemSave, audience: Audience, categoryIds: Seq[String]) = {
    val (existingAudience, audienceId) = db.withTransaction { implicit c =>
      dao.updateNewsItem(id, item)
      newsCategoryDao.deleteNewsCategories(id)
      newsCategoryDao.saveNewsCategories(id, categoryIds)

      val existingAudienceId = dao.getAudienceId(id)
      val existingAudience = existingAudienceId.map(audienceDao.getAudience)

      val audienceId = existingAudience match {
        case Some(existing) if existing == audience =>
          existingAudienceId.get
        case _ =>
          val audienceId = audienceDao.saveAudience(audience)
          dao.setAudienceId(id, audienceId)
          existingAudienceId.foreach(audienceDao.deleteAudience)

          audienceId
      }

      (existingAudience, audienceId)
    }

    Future {
      // Delete the recipients now and re-create them in the future
      if (existingAudience.nonEmpty) {
        db.withTransaction(implicit c => dao.setRecipients(id, Seq.empty))
      }

      schedulePublishNewsItem(id, audienceId, item.publishDate)
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
    *
    * @param newsItemId
    * @param recipients
    */
  override def setRecipients(newsItemId: String, recipients: Seq[Usercode]) =
  db.withTransaction(implicit c => dao.setRecipients(newsItemId, recipients))
}
