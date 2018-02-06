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

  val activityDao: ActivityDao = get[ActivityDao]
  val activityTagDao: ActivityTagDao = get[ActivityTagDao]
  val activityRecipientDao: ActivityRecipientDao = get[ActivityRecipientDao]
  val messagingDao: MessagingDao = get[MessagingDao]

  val activitySave: ActivitySave = Fixtures.activitySave.submissionDue

  val insertSkynetProvider =
    SQL"""
        INSERT INTO provider (id, display_name, icon, colour, publisher_id) VALUES
        ('skynet', 'Skynet', 'eye-o', 'greyish', 'default')
      """

  val deleteFixtureProvider =
    SQL"""
        DELETE FROM provider WHERE ID = ${activitySave.providerId}
       """

  val insertFixtureProvider =
    SQL"""
        INSERT INTO provider (id, display_name, icon, colour, publisher_id) VALUES
        (${activitySave.providerId}, 'Tabula display name', 'eye-o', 'greyish', 'default')
      """

  val deleteFixtureType =
    SQL"""
        DELETE FROM activity_type WHERE NAME = ${activitySave.`type`}
       """

  val insertFixtureType =
    SQL"""
        INSERT INTO activity_type (name, display_name) VALUES
        (${activitySave.`type`}, 'The display name')
      """

  val audienceId = "audience"

  "ActivityDao" should {

    "get activity by id" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      activityDao.getActivityById(activityId).map(_.id) mustBe Some(activityId)
    }

    "get activities by ids" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      activityDao.getActivitiesByIds(Seq(activityId)).map(_.id) mustBe Seq(activityId)
    }

    "replace activities" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      val newActivityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq(activityId))
      activityDao.getActivityById(activityId).flatMap(_.replacedBy) mustBe Some(newActivityId)
    }

    "find activities without tags" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      activityRecipientDao.create(activityId, "someone", None, shouldNotify=false)
      activityDao.getActivitiesForUser("someone", notifications = false).map(_.activity.id) must contain(activityId)
    }

    "find activities with tags" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      activityRecipientDao.create(activityId, "someone", None, shouldNotify=true)
      activityTagDao.save(activityId, ActivityTag("name", None, TagValue("value")))
      activityDao.getActivitiesForUser("someone", notifications = true).map(_.activity.id) must contain(activityId)
    }

    "get notifications since date" in transaction { implicit c =>
      val usercode = Usercode("someone")
      val nowDate = DateTime.now
      val oldDate = nowDate.minusMonths(1)
      val lastFetchedDate = nowDate.minusDays(1)

      val oldActivityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      val newActivityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)

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

      val activitySave = ActivitySave(Usercode("custard"), "default", "skynet", shouldNotify = false, "beady-eye", "Watching You", None, None, Seq.empty, Map.empty, None)
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      activityRecipientDao.create(activityId, "nicduke", None, shouldNotify = false)

      val response = activityDao.getActivitiesForUser("nicduke", notifications = false, limit = 1).head

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
      val id = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, AudienceSize.Finite(10), Nil)
      val id2 = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, AudienceSize.Finite(10), Nil)

      activityDao.setSentCount(id, 10)
      activityDao.setSentCount(id2, 10)

      activityDao.getPastActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must contain allOf(id, id2)
      activityDao.getFutureActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must be(empty)
    }

    "get sending activities created by a publisher" in transaction { implicit c =>
      val id = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, AudienceSize.Finite(10), Nil)
      val id2 = activityDao.save(Fixtures.activitySave.submissionDue, audienceId, AudienceSize.Finite(10), Nil)

      activityDao.setSentCount(id, 0)
      activityDao.setSentCount(id2, 5)

      activityDao.getPastActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must be(empty)
      activityDao.getSendingActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must contain allOf(id, id2)
      activityDao.getFutureActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must be(empty)
    }

    "get future activities created by publisher" in transaction { implicit c =>
      val id = activityDao.save(Fixtures.activitySave.submissionDue.copy(publishedAt = Some(DateTime.now.plusDays(1))), audienceId, AudienceSize.Finite(20), Nil)
      val id2 = activityDao.save(Fixtures.activitySave.submissionDue.copy(publishedAt = Some(DateTime.now.plusDays(2))), audienceId, AudienceSize.Finite(20), Nil)

      activityDao.getPastActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must be(empty)
      activityDao.getFutureActivitiesByPublisherId("elab", limit = 100).map(_.activity.id) must contain allOf(id, id2)
    }

    "delete an activity" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)

      activityDao.delete(activityId)

      SQL"SELECT COUNT(*) FROM ACTIVITY WHERE ID = $activityId"
        .executeQuery()
        .as(scalar[Int].single) must be(0)
    }

    "update an activity" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)

      activityDao.update(activityId, activitySave.copy(title = "New title"), audienceId, AudienceSize.Public)

      SQL"SELECT TITLE FROM ACTIVITY WHERE ID = $activityId"
        .executeQuery()
        .as(scalar[String].single) must be("New title")
    }

    "retrieve all tags associated with an activity" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      activityTagDao.save(activityId, ActivityTag("a", None, TagValue("apple")))
      activityTagDao.save(activityId, ActivityTag("b", None, TagValue("banana")))

      val maybeActivityRender = activityDao.getActivityRenderById(activityId)
      maybeActivityRender must not be empty

      val activityRender = maybeActivityRender.get
      activityRender.activity.id must be(activityId)
      activityRender.tags must have length 2
    }

    def createActivity(time: DateTime)(implicit c: Connection) = {
      val id = activityDao.save(activitySave.copy(publishedAt = Some(time)), audienceId, AudienceSize.Public, Nil)
      activityTagDao.save(id, ActivityTag("a", None, TagValue("apple")))
      activityTagDao.save(id, ActivityTag("b", None, TagValue("banana")))
      activityRecipientDao.create(id, "someone", Some(time), activitySave.shouldNotify)
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

      activityDao.getActivitiesForUser("someone", notifications = true, since = ids.headOption, before = ids.lastOption).map(_.activity.id) must be(Seq(ids(1), ids(2), ids(3)))
      activityDao.getActivitiesForUser("someone", notifications = true, since = ids.headOption, before = ids.lastOption, limit = 1).map(_.activity.id) must contain only ids(1)
    }

    "get activities before and since" in transaction { implicit c =>
      val first = createActivity(DateTime.now().minusHours(1))
      val middle = createActivity(DateTime.now())
      val last = createActivity(DateTime.now().plusHours(1))

      activityDao.getActivitiesForUser("someone", notifications = true, since = Some(first)).map(_.activity.id) must be(Seq(last, middle))
      activityDao.getActivitiesForUser("someone", notifications = true, before = Some(last)).map(_.activity.id) must be(Seq(middle, first))
      activityDao.getActivitiesForUser("someone", notifications = true, since = Some(first), before = Some(last)).map(_.activity.id) must contain only middle

      activityDao.getActivitiesForUser("someone", notifications = true, before = Some(last), limit = 1).map(_.activity.id) must contain only middle
      activityDao.getActivitiesForUser("someone", notifications = true, before = Some(middle), limit = 1).map(_.activity.id) must contain only first
      activityDao.getActivitiesForUser("someone", notifications = true, before = Some(first), limit = 1) must be(empty)

      activityDao.getActivitiesForUser("someone", notifications = true, since = Some(first), before = Some(first)) must be(empty)
    }

    "count notifications since date" in transaction { implicit c =>
      activityDao.countNotificationsSinceDate("someone", DateTime.now.minusHours(2)) must be(0)

      val activityId = activityDao.save(Fixtures.activitySave.submissionDue.copy(publishedAt = Some(DateTime.now.minusHours(1))), audienceId, AudienceSize.Public, Nil)
      activityRecipientDao.create(activityId, "someone", None, shouldNotify = true)

      activityDao.countNotificationsSinceDate("someone", DateTime.now.minusHours(2)) must be(1)
    }

    "retrieve provider" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      deleteFixtureProvider.execute()
      insertFixtureProvider.execute()

      val maybeActivityRender = activityDao.getActivityRenderById(activityId)
      maybeActivityRender must not be empty

      val activityRender = maybeActivityRender.get
      activityRender.activity.id must be(activityId)
      activityRender.provider.displayName must not be empty
    }

    "retrieve type" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)
      deleteFixtureType.execute()
      insertFixtureType.execute()

      val maybeActivityRender = activityDao.getActivityRenderById(activityId)
      maybeActivityRender must not be empty

      val activityRender = maybeActivityRender.get
      activityRender.activity.id must be(activityId)
      activityRender.`type`.displayName must not be empty
    }

    "api" in transaction { implicit c =>
      val activityId = activityDao.save(Fixtures.activitySave.activityFromApi.copy(publishedAt = Some(DateTime.now.minusHours(1))), audienceId, AudienceSize.Public, Nil)
      val saved = activityDao.getActivityById(activityId)
      saved must not be empty
      saved.get.api mustBe true
    }

    "get recipient read count for activity id" in transaction { implicit c =>
      val now = DateTime.now
      val usercodeOne = "usercodeOne"
      val usercodeTwo = "usercodeTwo"
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)

      SQL(s"""
      INSERT INTO activity_recipient VALUES
      ('$activityId', '$usercodeOne', {threeDaysAgo}, null, null, null, {threeDaysAgo}, 1, 0),
      ('$activityId', '$usercodeTwo', {threeDaysAgo}, null, null, null, {threeDaysAgo}, 1, 0)
        """)
        .on('threeDaysAgo -> now.minusDays(3), 'usercodeOne -> usercodeOne, 'usercodeTwo -> usercodeTwo)
        .execute()

      SQL(s"INSERT INTO activity_recipient_read VALUES ('$usercodeOne', {now}), ('$usercodeTwo', {fiveDaysAgo})")
        .on('now -> now, 'fiveDaysAgo -> now.minusDays(4))
        .execute()

      val count: Int = activityDao.getActivityReadCountSincePublishedDate(activityId)

      count mustBe 1
    }
  }
}
