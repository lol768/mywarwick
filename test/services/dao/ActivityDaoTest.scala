package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import helpers.{Fixtures, OneStartAppPerSuite}
import models.Output.Mobile
import models._
import org.joda.time.DateTime
import helpers.BaseSpec
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

class ActivityDaoTest extends BaseSpec with OneStartAppPerSuite {

  val activityDao = get[ActivityDao]
  val activityTagDao = get[ActivityTagDao]
  val activityRecipientDao = get[ActivityRecipientDao]
  val messagingDao = get[MessagingDao]

  val activitySave = Fixtures.activitySave.submissionDue

  val insertSkynetProvider =
    SQL"""
        INSERT INTO provider (id, display_name, icon, colour, publisher_id) VALUES
        ('skynet', 'Skynet', 'eye-o', 'greyish', 'default')
      """

  val audienceId = "audience"

  "ActivityDao" should {

    "get activity by id" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      activityDao.getActivityById(activityId).map(_.id) mustBe Some(activityId)
    }

    "get activities by ids" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      activityDao.getActivitiesByIds(Seq(activityId)).map(_.id) mustBe Seq(activityId)
    }

    "replace activities" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      val newActivityId = activityDao.save(activitySave, audienceId, Seq(activityId))
      activityDao.getActivityById(activityId).flatMap(_.replacedBy) mustBe Some(newActivityId)
    }

    "find activities without tags" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      activityRecipientDao.create(activityId, "someone", None, shouldNotify=false)
      activityDao.getActivitiesForUser("someone").map(_.activity.id) must contain(activityId)
    }

    "find activities with tags" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      activityRecipientDao.create(activityId, "someone", None, shouldNotify=true)
      activityTagDao.save(activityId, ActivityTag("name", TagValue("value")))
      activityDao.getActivitiesForUser("someone").map(_.activity.id) must contain(activityId)
    }

    "get notifications since date" in transaction { implicit c =>
      val usercode = Usercode("someone")
      val nowDate = DateTime.now
      val oldDate = nowDate.minusMonths(1)
      val lastFetchedDate = nowDate.minusDays(1)

      val oldActivityId = activityDao.save(activitySave, audienceId, Seq.empty)
      val newActivityId = activityDao.save(activitySave, audienceId, Seq.empty)

      val newActivity = activityDao.getActivityById(newActivityId).get

      SQL(
        """
      INSERT INTO activity_recipient VALUES
      ({oldActivityId}, {usercode}, {oldDate}, null, null, null, {oldDate}, 1, 0),
      ({newActivityId}, {usercode}, {nowDate}, null, null, null, {nowDate}, 1, 0)
        """)
        .on(
          'oldActivityId -> oldActivityId,
          'newActivityId -> newActivityId,
          'usercode -> usercode.string,
          'oldDate -> oldDate,
          'nowDate -> nowDate
        ).execute()

      messagingDao.save(newActivity, usercode, Mobile)

      activityDao.getPushNotificationsSinceDate(usercode.string, lastFetchedDate) mustBe Seq(newActivity)
    }

    "update last-read date" in transaction { implicit c =>
      val usercode = "someone"

      def lastReadDate: DateTime =
        SQL("SELECT NOTIFICATIONS_LAST_READ FROM ACTIVITY_RECIPIENT_READ WHERE USERCODE={usercode}")
          .on('usercode -> usercode)
          .as(scalar[DateTime].singleOpt).orNull

      val now = DateTime.now
      val yesterday = now.minusDays(1)

      lastReadDate mustBe null

      activityDao.saveLastReadDate(usercode, yesterday)
      lastReadDate mustBe yesterday

      activityDao.saveLastReadDate(usercode, now)
      lastReadDate mustBe now

      activityDao.saveLastReadDate(usercode, yesterday)
      lastReadDate mustBe now
    }

    "get provider's activity icon" in transaction { implicit c =>
      insertSkynetProvider.execute()

      val activitySave = ActivitySave(Usercode("custard"), "default", "skynet", false, "beady-eye", "Watching You", None, None, Seq.empty, Map.empty, None)
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      activityRecipientDao.create(activityId, "nicduke", None, false)

      val response = activityDao.getActivitiesForUser("nicduke", limit = 1).head

      response.icon mustBe Some(ActivityIcon("eye-o", Some("greyish")))
    }

    "get missing activity icon" in transaction { implicit t =>
      activityDao.getActivityIcon("nonexist") mustBe None
    }

    "get activity icon" in transaction { implicit t =>
      insertSkynetProvider.execute()
      activityDao.getActivityIcon("skynet").get.colour.get mustBe "greyish"
    }

    "get past activities created by publisher" in transaction { implicit c =>
      val id = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, Nil)
      val id2 = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, Nil)

      activityDao.getPastActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must contain allOf(id, id2)
      activityDao.getFutureActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must be(empty)
    }

    "get future activities created by publisher" in transaction { implicit c =>
      val id = activityDao.save(Fixtures.activitySave.submissionDue.copy(publishedAt = Some(DateTime.now.plusDays(1))), audienceId, Nil)
      val id2 = activityDao.save(Fixtures.activitySave.submissionDue.copy(publishedAt = Some(DateTime.now.plusDays(2))), audienceId, Nil)

      activityDao.getPastActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must be(empty)
      activityDao.getFutureActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must contain allOf(id, id2)
    }

    "delete an activity" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)

      activityDao.delete(activityId)

      SQL"SELECT COUNT(*) FROM ACTIVITY WHERE ID = $activityId"
        .executeQuery()
        .as(scalar[Int].single) must be(0)
    }

    "update an activity" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)

      activityDao.update(activityId, activitySave.copy(title = "New title"), audienceId)

      SQL"SELECT TITLE FROM ACTIVITY WHERE ID = $activityId"
        .executeQuery()
        .as(scalar[String].single) must be("New title")
    }

    "retrieve all tags associated with an activity" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      activityTagDao.save(activityId, ActivityTag("a", TagValue("apple")))
      activityTagDao.save(activityId, ActivityTag("b", TagValue("banana")))

      val maybeActivityRender = activityDao.getActivityRenderById(activityId)
      maybeActivityRender must not be empty

      val activityRender = maybeActivityRender.get
      activityRender.activity.id must be(activityId)
      activityRender.tags must have length 2
    }

    def createActivity(time: DateTime)(implicit c: Connection) = {
      val id = activityDao.save(activitySave.copy(publishedAt = Some(time)), audienceId, Nil)
      activityTagDao.save(id, ActivityTag("a", TagValue("apple")))
      activityTagDao.save(id, ActivityTag("b", TagValue("banana")))
      activityRecipientDao.create(id, "someone", Some(time), false)
      id
    }

    "handle edge case where multiple activities created at the same instant" in transaction { implicit c =>
    val time = DateTime.now()

      val ids = Seq(
        createActivity(time),
        createActivity(time),
        createActivity(time),
        createActivity(time),
        createActivity(time)
      ).sorted

      activityDao.getActivitiesForUser("someone", since = ids.headOption, before = ids.lastOption).map(_.activity.id) must be(Seq(ids(1), ids(2), ids(3)))
      activityDao.getActivitiesForUser("someone", since = ids.headOption, before = ids.lastOption, limit = 1).map(_.activity.id) must contain only ids(1)
    }

    "get activities before and since" in transaction { implicit c =>
      val first = createActivity(DateTime.now().minusHours(1))
      val middle = createActivity(DateTime.now())
      val last = createActivity(DateTime.now().plusHours(1))

      activityDao.getActivitiesForUser("someone", since = Some(first)).map(_.activity.id) must be(Seq(last, middle))
      activityDao.getActivitiesForUser("someone", before = Some(last)).map(_.activity.id) must be(Seq(middle, first))
      activityDao.getActivitiesForUser("someone", since = Some(first), before = Some(last)).map(_.activity.id) must contain only middle

      activityDao.getActivitiesForUser("someone", before = Some(last), limit = 1).map(_.activity.id) must contain only middle
      activityDao.getActivitiesForUser("someone", before = Some(middle), limit = 1).map(_.activity.id) must contain only first
      activityDao.getActivitiesForUser("someone", before = Some(first), limit = 1) must be(empty)

      activityDao.getActivitiesForUser("someone", since = Some(first), before = Some(first)) must be(empty)
    }
  }
}
