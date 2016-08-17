package services.dao

import helpers.{Fixtures, OneStartAppPerSuite}
import models._
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import Output.Mobile
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode
import anorm._
import anorm.SqlParser._

class ActivityDaoTest extends PlaySpec with OneStartAppPerSuite {

  val activityDao = get[ActivityDao]
  val activityTagDao = get[ActivityTagDao]
  val activityRecipientDao = get[ActivityRecipientDao]
  val messagingDao = get[MessagingDao]

  val activitySave = Fixtures.activitySave.submissionDue

  val insertSkynetProvider = SQL"""
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
      activityRecipientDao.create(activityId, "someone", None)
      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)
    }

    "find activities with tags" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      activityRecipientDao.create(activityId, "someone", None)
      activityTagDao.save(activityId, ActivityTag("name", TagValue("value")))
      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)
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
      ({oldActivityId}, {usercode}, {oldDate}, null, null, null, {oldDate}),
      ({newActivityId}, {usercode}, {nowDate}, null, null, null, {nowDate})
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
      activityRecipientDao.create(activityId, "nicduke", None)

      val response = activityDao.getActivitiesForUser("nicduke", 1).head

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
  }
}
