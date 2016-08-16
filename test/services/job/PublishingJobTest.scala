package services.job

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import warwick.anorm.converters.ColumnConversions._
import helpers.{Fixtures, MockSchedulerService, OneStartAppPerSuite}
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.quartz.{JobDataMap, JobDetail, JobExecutionContext, Scheduler}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
import services._
import services.dao._
import services.messaging.MessagingService
import warwick.sso.Usercode

import scala.util.Try

class PublishingJobTest extends PlaySpec with MockitoSugar with OneStartAppPerSuite {

  val db = get[Database]
  val audienceService = mock[AudienceService]
  val scheduler = mock[SchedulerService]

  val activityIdKey = "activityId"
  val audienceId = "audienceId"

  val context = mock[JobExecutionContext]
  val jobDetail = mock[JobDetail]
  val map = mock[JobDataMap]

  when(context.getJobDetail).thenReturn(jobDetail)
  when(jobDetail.getJobDataMap).thenReturn(map)
  when(map.getString(audienceId)).thenReturn(audienceId)

  val dave = Usercode("dave")
  val james = Usercode("james")
  val recipients = Try(Seq(dave, james))
  when(audienceService.resolve(any())).thenReturn(recipients)

  val date = DateTime.now

  private def saveNewsItem(id: String, publishDate: DateTime)(implicit c: Connection) =
    SQL"""
        INSERT INTO NEWS_ITEM (ID, TITLE, TEXT, CREATED_AT, PUBLISH_DATE, IGNORE_CATEGORIES, PUBLISHER_ID, CREATED_BY)
        VALUES ($id, 'balls', 'balls', $publishDate, $publishDate, 1, 'default', 'balls')
      """.executeInsert()

  "PublishNewsItemJob" should {

    val newsDao = get[NewsDao]
    val newsCategoryDao = mock[NewsCategoryDao]
    val audienceDao = mock[AudienceDao]
    val userInitialisationService = mock[UserInitialisationService]
    val newsService = new AnormNewsService(db, newsDao, audienceService, newsCategoryDao, audienceDao, userInitialisationService, scheduler)

    val newsItemId = "newsItemId"
    when(map.getString(newsItemId)).thenReturn(newsItemId)
    val publishNewsItemJob = new PublishNewsItemJob(audienceService, newsService, scheduler)

    "save audience for news item" in db.withConnection { implicit c =>

      saveNewsItem(newsItemId, date)

      publishNewsItemJob.execute(context)

      val recipientsSet = SQL"SELECT usercode FROM news_recipient WHERE news_item_id=$newsItemId"
        .as((str("usercode") map { u => Usercode(u) }).*)

      recipientsSet mustBe recipients.get
    }
  }

  "PublishNotificationJob" should {

    val activityDao = get[ActivityDao]
    val recipientDao = get[ActivityRecipientDao]
    val tagDao = mock[ActivityTagDao]
    val activityTypeService = mock[ActivityTypeService]
    val messaging = mock[MessagingService]
    val pubSub = mock[PubSub]
    val audienceDao = mock[AudienceDao]
    val scheduler = mock[SchedulerService]
    val activityService = new ActivityServiceImpl(db, activityDao, activityTypeService, tagDao, audienceDao, recipientDao, scheduler)

    val publishNotificationJob = new PublishActivityJob(audienceService, activityService, messaging, pubSub, scheduler)

    "save audience for notification" in {

      db.withConnection { implicit c =>
        val activitySave = Fixtures.activitySave.submissionDue.copy(publishedAt = Some(date))

        val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
        when(map.getString(activityIdKey)).thenReturn(activityId)

        publishNotificationJob.execute(context)

        val activity = activityDao.getActivityById(activityId).get

        verify(messaging).send(recipients.get.toSet, activity)
        verify(pubSub).publish(Matchers.eq(dave.string), any())
        verify(pubSub).publish(Matchers.eq(james.string), any())

        getRecipients(activity.id) mustBe recipients.get

        publishNotificationJob.execute(context)

        // check recipients are replaced, not appended.
        getRecipients(activity.id) mustBe recipients.get
      }
    }
  }

  def getRecipients(activityId: String)(implicit c: Connection) =
    SQL"SELECT usercode FROM activity_recipient WHERE activity_id = $activityId"
      .as(scalar[String].map(Usercode.apply).*)
}
