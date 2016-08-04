package services.job

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import warwick.anorm.converters.ColumnConversions._
import helpers.{Fixtures, MockScheduleJobService, OneStartAppPerSuite}
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.quartz.{JobDataMap, JobDetail, JobExecutionContext}
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
  val scheduler = new MockScheduleJobService

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

  "NewsAudienceResolverJob" should {

    val newsDao = get[NewsDao]
    val newsCategoryDao = mock[NewsCategoryDao]
    val audienceDao = mock[AudienceDao]
    val userInitialisationService = mock[UserInitialisationService]
    val newsService = new AnormNewsService(db, newsDao, audienceService, newsCategoryDao, audienceDao, userInitialisationService, scheduler)

    val newsItemId = "newsItemId"
    when(map.getString(newsItemId)).thenReturn(newsItemId)
    val newsAudienceResolverJob = new PublishNewsItemJob(audienceService, newsService, scheduler)

    "save audience for news item" in db.withConnection { implicit c =>

      saveNewsItem(newsItemId, date)

      newsAudienceResolverJob.execute(context)

      val recipientsSet = SQL"SELECT usercode FROM news_recipient WHERE news_item_id=$newsItemId"
        .as((str("usercode") map { u => Usercode(u) }).*)

      recipientsSet mustBe recipients.get
    }
  }

  "NotificationsAudienceResolverJob" should {

    val activityDao = get[ActivityDao]
    val creationDao = mock[ActivityCreationDao]
    val tagDao = mock[ActivityTagDao]
    val activityTypeService = mock[ActivityTypeService]
    val messaging = mock[MessagingService]
    val pubSub = mock[PubSub]
    val activityService = new ActivityServiceImpl(activityDao, creationDao, tagDao, messaging, pubSub, db, activityTypeService)

    val notificationsAudienceResolverJob = new PublishActivityJob(audienceService, activityService, messaging, pubSub, scheduler)

    "save audience for notification" in {

      db.withConnection { implicit c =>

        val activitySave = Fixtures.activitySave.submissionDue.copy(audienceId = Some(audienceId), generatedAt = Some(date))

        val activityId = activityDao.save(activitySave, Seq.empty)
        when(map.getString(activityIdKey)).thenReturn(activityId)

        notificationsAudienceResolverJob.execute(context)

        val activity = activityDao.getActivityById(activityId).get

        verify(messaging).send(recipients.get.toSet, activity)
        verify(pubSub).publish(Matchers.eq(dave.string), any())
        verify(pubSub).publish(Matchers.eq(james.string), any())
      }
    }
  }
}
