package services.job

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import helpers.{BaseSpec, Fixtures, OneStartAppPerSuite}
import models.{Audience, AudienceSize}
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.quartz.{JobDataMap, JobDetail, JobExecutionContext}
import org.scalatest.mockito.MockitoSugar
import play.api.db.Database
import services._
import services.dao._
import services.messaging.MessagingService
import warwick.sso.{UserLookupService, Usercode}

class PublishingJobTest extends BaseSpec with MockitoSugar with OneStartAppPerSuite {

  private val db = get[Database]
  private val audienceService = get[AudienceService]
  private val audienceDao = get[AudienceDao]
  private val pubSub = mock[PubSub]
  private val scheduler = mock[SchedulerService]

  private val context = mock[JobExecutionContext]
  private val jobDetail = mock[JobDetail]
  private val jobDataMap = mock[JobDataMap]

  val cache = new MockCacheApi

  when(context.getJobDetail).thenReturn(jobDetail)
  when(jobDetail.getJobDataMap).thenReturn(jobDataMap)

  "PublishNewsItemJob" should {

    val newsDao = get[NewsDao]
    val newsCategoryDao = get[NewsCategoryDao]
    val newsImageDao = mock[NewsImageDao]
    val userInitialisationService = mock[UserInitialisationService]
    val userLookupService = mock[UserLookupService]
    val newsService = new AnormNewsService(db, newsDao, audienceService, newsCategoryDao, newsImageDao, audienceDao, userInitialisationService, scheduler, userLookupService, cache)

    val publishNewsItemJob = new PublishNewsItemJob(audienceService, newsService, scheduler)

    "save audience for news item" in db.withConnection { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience(Seq(
        Audience.UsercodesAudience(Set(Usercode("dave"), Usercode("james")))
      )))
      val newsItemId = newsDao.save(Fixtures.news.save(), audienceId, AudienceSize.Finite(2))

      when(jobDataMap.getString("newsItemId")).thenReturn(newsItemId)
      when(jobDataMap.getString("audienceId")).thenReturn(audienceId)

      publishNewsItemJob.execute(context)

      val recipientsSet = SQL"SELECT usercode FROM news_recipient WHERE news_item_id=$newsItemId"
        .as(str("usercode").*)

      recipientsSet must contain allOf("dave", "james")

      val newsItemAudit = newsDao.getNewsAuditByIds(Seq(newsItemId))
      newsItemAudit.head.audienceSize mustBe AudienceSize.Finite(2)
    }

    "save public audience for news item" in db.withConnection { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience.Public)
      val newsItemId = newsDao.save(Fixtures.news.save(), audienceId, AudienceSize.Public)

      when(jobDataMap.getString("newsItemId")).thenReturn(newsItemId)
      when(jobDataMap.getString("audienceId")).thenReturn(audienceId)

      publishNewsItemJob.execute(context)

      val recipientsSet = SQL"SELECT usercode FROM news_recipient WHERE news_item_id=$newsItemId"
        .as(str("usercode").*)

      recipientsSet must contain only "*"

      val newsItemAudit = newsDao.getNewsAuditByIds(Seq(newsItemId))
      newsItemAudit.head.audienceSize mustBe AudienceSize.Public
    }
  }

  "PublishNotificationJob" should {

    val activityTypeService = get[ActivityTypeService]
    val activityDao = get[ActivityDao]
    val recipientDao = get[ActivityRecipientDao]
    val tagDao = get[ActivityTagDao]
    val activityMuteDao = get[ActivityMuteDao]
    val messaging = mock[MessagingService]
    val activityService = new ActivityServiceImpl(db, activityDao, activityTypeService, tagDao, audienceService, audienceDao, recipientDao, activityMuteDao, scheduler)

    val publishNotificationJob = new PublishActivityJob(audienceService, activityService, messaging, pubSub, scheduler)

    "save audience for notification" in {

      db.withConnection { implicit c =>
        val audienceId = audienceDao.saveAudience(Audience(Seq(
          Audience.UsercodesAudience(Set(Usercode("dave"), Usercode("james")))
        )))
        when(jobDataMap.getString("audienceId")).thenReturn(audienceId)

        val activityId = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, AudienceSize.Public, Seq.empty)
        when(jobDataMap.getString("activityId")).thenReturn(activityId)

        publishNotificationJob.execute(context)

        val activity = activityDao.getActivityById(activityId).get

        val recipients = Seq(Usercode("dave"), Usercode("james"))
        verify(messaging).send(recipients.toSet, activity)
        verify(pubSub).publish(Matchers.eq("dave"), any())
        verify(pubSub).publish(Matchers.eq("james"), any())

        getRecipients(activity.id) must contain allOf("dave", "james")

        publishNotificationJob.execute(context)

        // check recipients are replaced, not appended.
        getRecipients(activity.id).toSet mustBe Set("dave", "james")
      }
    }
  }

  def getRecipients(activityId: String)(implicit c: Connection): List[String] =
    SQL"SELECT usercode FROM activity_recipient WHERE activity_id = $activityId"
      .as(str("usercode").*)
}
