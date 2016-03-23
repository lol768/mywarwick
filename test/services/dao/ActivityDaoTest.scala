package services.dao

import helpers.{Fixtures, OneStartAppPerSuite}
import models.{ActivityTag, Output, TagValue}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import Output.Mobile
import anorm.SQL
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode
import anorm.SqlParser._

class ActivityDaoTest extends PlaySpec with OneStartAppPerSuite {

  val activityDao = app.injector.instanceOf[ActivityDao]
  val activityTagDao = app.injector.instanceOf[ActivityTagDao]
  val activityRecipientDao = app.injector.instanceOf[ActivityRecipientDao]
  val messagingDao = app.injector.instanceOf[MessagingDao]

  val activityPrototype = Fixtures.activityPrototype.submissionDue


  "ActivityDao" should {

    "get activity by id" in transaction { implicit c =>

      val activityId = activityDao.save(activityPrototype, Seq.empty)

      activityDao.getActivityById(activityId).map(_.id) must contain(activityId)

    }

    "get activities by ids" in transaction { implicit c =>

      val activityId = activityDao.save(activityPrototype, Seq.empty)

      activityDao.getActivitiesByIds(Seq(activityId)).map(_.id) must contain(activityId)

    }

    "replace activities" in transaction { implicit c =>

      val activityId = activityDao.save(activityPrototype, Seq.empty)
      val newActivityId = activityDao.save(activityPrototype, Seq(activityId))

      activityDao.getActivityById(activityId).flatMap(_.replacedBy) mustBe Some(newActivityId)

    }

    "find activities without tags" in transaction { implicit c =>

      val activityId = activityDao.save(activityPrototype, Seq.empty)
      activityRecipientDao.create(activityId, "someone", None)

      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)

    }

    "find activities with tags" in transaction { implicit c =>

      val activityId = activityDao.save(activityPrototype, Seq.empty)
      activityRecipientDao.create(activityId, "someone", None)
      activityTagDao.save(activityId, ActivityTag("name", TagValue("value")))

      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)

    }

    "get notifications since date" in transaction { implicit c =>
      val usercode = Usercode("someone")
      val nowDate = DateTime.now
      val oldDate = nowDate.minusMonths(1)
      val lastFetchedDate = nowDate.minusDays(1)

      val oldActivityId = activityDao.save(activityPrototype, Seq.empty)
      val newActivityId = activityDao.save(activityPrototype, Seq.empty)

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
  }
}
