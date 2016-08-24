package services.job

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import helpers.{Fixtures, OneStartAppPerSuite}
import models.Audience
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.quartz.{JobDataMap, JobDetail, JobExecutionContext}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
import services._
import services.dao._
import services.messaging.MessagingService
import warwick.sso.Usercode

class PublishingJobTest extends PlaySpec with MockitoSugar with OneStartAppPerSuite {

  val db = get[Database]
  val audienceService = get[AudienceService]
  val audienceDao = get[AudienceDao]
  val pubSub = mock[PubSub]
  val scheduler = mock[SchedulerService]

  val context = mock[JobExecutionContext]
  val jobDetail = mock[JobDetail]
  val jobDataMap = mock[JobDataMap]

  when(context.getJobDetail).thenReturn(jobDetail)
  when(jobDetail.getJobDataMap).thenReturn(jobDataMap)

  "PublishNewsItemJob" should {

    val newsDao = get[NewsDao]
    val newsCategoryDao = get[NewsCategoryDao]
    val userInitialisationService = mock[UserInitialisationService]
    val newsService = new AnormNewsService(db, newsDao, audienceService, newsCategoryDao, audienceDao, userInitialisationService, scheduler)

    val publishNewsItemJob = new PublishNewsItemJob(audienceService, newsService, scheduler)

    "save audience for news item" in db.withConnection { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience(Seq(
        Audience.UsercodeAudience(Usercode("dave")),
        Audience.UsercodeAudience(Usercode("james"))
      )))
      val newsItemId = newsDao.save(Fixtures.news.save(), audienceId)

      when(jobDataMap.getString("newsItemId")).thenReturn(newsItemId)
      when(jobDataMap.getString("audienceId")).thenReturn(audienceId)

      publishNewsItemJob.execute(context)

      val recipientsSet = SQL"SELECT usercode FROM news_recipient WHERE news_item_id=$newsItemId"
        .as(str("usercode").*)

      recipientsSet must contain allOf("dave", "james")
    }

    "save public audience for news item" in db.withConnection { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience.Public)
      val newsItemId = newsDao.save(Fixtures.news.save(), audienceId)

      when(jobDataMap.getString("newsItemId")).thenReturn(newsItemId)
      when(jobDataMap.getString("audienceId")).thenReturn(audienceId)

      publishNewsItemJob.execute(context)

      val recipientsSet = SQL"SELECT usercode FROM news_recipient WHERE news_item_id=$newsItemId"
        .as(str("usercode").*)

      recipientsSet must contain only("*")
    }
  }

  "PublishNotificationJob" should {

    val activityTypeService = get[ActivityTypeService]
    val activityDao = get[ActivityDao]
    val recipientDao = get[ActivityRecipientDao]
    val tagDao = get[ActivityTagDao]
    val messaging = mock[MessagingService]
    val activityService = new ActivityServiceImpl(db, activityDao, activityTypeService, tagDao, audienceDao, recipientDao, scheduler)

    val publishNotificationJob = new PublishActivityJob(audienceService, activityService, messaging, pubSub, scheduler)

    "save audience for notification" in {

      db.withConnection { implicit c =>
        val audienceId = audienceDao.saveAudience(Audience(Seq(
          Audience.UsercodeAudience(Usercode("dave")),
          Audience.UsercodeAudience(Usercode("james"))
        )))
        when(jobDataMap.getString("audienceId")).thenReturn(audienceId)

        val activityId = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, Seq.empty)
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

  def getRecipients(activityId: String)(implicit c: Connection) =
    SQL"SELECT usercode FROM activity_recipient WHERE activity_id = $activityId"
      .as(str("usercode").*)
}
