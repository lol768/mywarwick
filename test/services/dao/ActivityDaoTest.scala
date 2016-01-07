package services.dao

import helpers.{Fixtures, OneStartAppPerSuite}
import models.{ActivityTag, TagValue}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import warwick.anorm.converters.ColumnConversions._

class ActivityDaoTest extends PlaySpec with OneStartAppPerSuite {

  val activityDao = app.injector.instanceOf[ActivityDao]
  val activityTagDao = app.injector.instanceOf[ActivityTagDao]
  val activityRecipientDao = app.injector.instanceOf[ActivityRecipientDao]
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
      val usercode = "someone"
      val nowDate = DateTime.now
      val oldDate = nowDate.minusMonths(1)
      val lastFetchedDate = nowDate.minusDays(1)

      val oldActivityId = activityDao.save(activityPrototype, Seq.empty)
      val newActivityId = activityDao.save(activityPrototype, Seq.empty)

      val newActivity = activityDao.getActivityById(newActivityId).get

      anorm.SQL(
        """
      INSERT INTO activity_recipient VALUES
      ({oldActivityId}, {usercode}, {oldDate}, null, null, null, {oldDate}),
      ({newActivityId}, {usercode}, {nowDate}, null, null, null, {nowDate})
        """)
        .on(
          'oldActivityId -> oldActivityId,
          'newActivityId -> newActivityId,
          'usercode -> usercode,
          'oldDate -> oldDate,
          'nowDate -> nowDate
        ).execute()

      activityDao.getNotificationsSinceDate(usercode, lastFetchedDate) mustBe Seq(newActivity)
    }
  }
}
