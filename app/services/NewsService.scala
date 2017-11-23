package services

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.Audience.DepartmentSubset
import models.news.{NewsItemRender, NewsItemRenderWithAuditAndAudience, NewsItemSave}
import models.{Audience, AudienceSize}
import org.joda.time.DateTime
import org.quartz.JobBuilder.newJob
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import org.quartz._
import play.api.cache.{CacheApi, SyncCacheApi}
import play.api.db.Database
import services.dao.{AudienceDao, NewsCategoryDao, NewsDao, NewsImageDao}
import services.job.PublishNewsItemJob
import system.ThreadPools.web
import warwick.sso.{GroupName, UserLookupService, Usercode}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

@ImplementedBy(classOf[AnormNewsService])
trait NewsService {
  def getAudience(newsId: String): Option[Audience]

  def getNewsByPublisher(publisherId: String, limit: Int, offset: Int = 0): Seq[NewsItemRender]

  def getNewsByPublisherWithAuditsAndAudience(publisherId: String, limit: Int, offset: Int = 0): Seq[NewsItemRenderWithAuditAndAudience]

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

  def getNewsItem(id: String): Option[NewsItemRender]

  def setRecipients(newsItemId: String, recipients: Set[Usercode]): Unit

  def delete(newsItemId: String): Unit

  def getNewsItemsMatchingAudience(webGroup: Option[GroupName], departmentCode: Option[String], departmentSubset: Option[DepartmentSubset], publisherId: Option[String], limit: Int): Seq[NewsItemRender]

  def updateAudienceCount(id: String): Unit
}

class AnormNewsService @Inject()(
  db: Database,
  dao: NewsDao,
  audienceService: AudienceService,
  newsCategoryDao: NewsCategoryDao,
  newsImageDao: NewsImageDao,
  audienceDao: AudienceDao,
  userInitialisationService: UserInitialisationService,
  scheduler: SchedulerService,
  userLookupService: UserLookupService,
  cache: SyncCacheApi
) extends NewsService {

  override def delete(newsId: String): Unit = db.withTransaction { implicit c =>
    newsImageDao.deleteForNewsItemId(newsId)
    newsCategoryDao.deleteNewsCategories(newsId)
    dao.deleteRecipients(newsId)
    dao.delete(newsId)
  }

  override def getNewsItemsMatchingAudience(webGroup: Option[GroupName], departmentCode: Option[String], departmentSubset: Option[DepartmentSubset], publisherId: Option[String], limit: Int): Seq[NewsItemRender] =
    cache.getOrElseUpdate(s"audienceNews:$webGroup:$departmentCode:$departmentSubset:$publisherId:$limit", Duration(10, TimeUnit.MINUTES)) {
      db.withConnection(implicit c =>
        dao.getNewsByIds(dao.getNewsItemsMatchingAudience(webGroup, departmentCode, departmentSubset, publisherId, limit))
      )
    }

  override def getNewsByPublisher(publisherId: String, limit: Int, offset: Int): Seq[NewsItemRender] =
    db.withConnection { implicit c =>
      dao.allNews(publisherId, limit, offset)
    }

  def getNewsByPublisherWithAuditsAndAudience(publisherId: String, limit: Int, offset: Int = 0): Seq[NewsItemRenderWithAuditAndAudience] = {
    val news = getNewsByPublisher(publisherId, limit, offset)
    val audits = db.withConnection { implicit c =>
      val audits = dao.getNewsAuditByIds(news.map(_.id))
      val users = userLookupService.getUsers(audits.flatMap(n => Seq(n.createdBy, n.updatedBy).flatten)).getOrElse(Map.empty)
      audits.map(audit => audit.copy(
        createdBy = audit.createdBy.flatMap(u => users.get(u)),
        updatedBy = audit.updatedBy.flatMap(u => users.get(u))
      ))
    }.groupBy(_.id).mapValues(_.head)
    val audiences = news.map(n => n.id -> getAudience(n.id).getOrElse(Audience())).toMap
    news.map(n => NewsItemRenderWithAuditAndAudience.applyWithAudit(n, audits(n.id), audiences(n.id)))
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
      val audienceSize = audience match {
        case Audience.Public => AudienceSize.Public
        case _ => AudienceSize.Finite(audienceService.resolve(audience).toOption.map(_.size).getOrElse(0))
      }
      val id = dao.save(item, audienceId, audienceSize)
      newsCategoryDao.saveNewsCategories(id, categoryIds)
      schedulePublishNewsItem(id, audienceId, item.publishDate)
      id
    }

  override def update(id: String, item: NewsItemSave, audience: Audience, categoryIds: Seq[String]): Future[Unit] = {
    val (existingAudience, audienceId) = db.withTransaction { implicit c =>
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

      val audienceSize = audience match {
        case Audience.Public => AudienceSize.Public
        case _ => AudienceSize.Finite(audienceService.resolve(audience).toOption.map(_.size).getOrElse(0))
      }

      dao.updateNewsItem(id, item, audienceSize)
      newsCategoryDao.deleteNewsCategories(id)
      newsCategoryDao.saveNewsCategories(id, categoryIds)

      (existingAudience, audienceId)
    }

    Future {
      // Delete the recipients now and re-create them in the future
      if (existingAudience.nonEmpty) {
        db.withTransaction(implicit c => dao.setRecipients(id, Set.empty))
      }

      schedulePublishNewsItem(id, audienceId, item.publishDate)
    }
  }

  override def getAudience(newsId: String): Option[Audience] =
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
  override def setRecipients(newsItemId: String, recipients: Set[Usercode]): Unit =
    db.withTransaction(implicit c => dao.setRecipients(newsItemId, recipients))

  override def updateAudienceCount(id: String): Unit =
    db.withTransaction { implicit c =>
      val audienceSize = dao.getAudienceId(id).map(audienceDao.getAudience) match {
        case Some(Audience.Public) => AudienceSize.Public
        case Some(audience) => AudienceSize.Finite(audienceService.resolve(audience).toOption.map(_.size).getOrElse(0))
        case _ => AudienceSize.Finite(0)
      }
      dao.updateAudienceCount(id, audienceSize)
    }
}
