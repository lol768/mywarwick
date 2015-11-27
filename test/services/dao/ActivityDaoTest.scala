package services.dao

import helpers.OneStartAppPerSuite
import helpers.TestObjectFactory._
import models.{ActivityTag, TagValue}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.db.Database

class ActivityDaoTest extends PlaySpec with OneStartAppPerSuite {

  implicit val c = app.injector.instanceOf[Database].getConnection()

  val activityDao = app.injector.instanceOf[ActivityDao]
  val activityTagDao = app.injector.instanceOf[ActivityTagDao]
  val activityRecipientDao = app.injector.instanceOf[ActivityRecipientDao]

  "ActivityDao" should {

    "get activity by id" in {

      val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)

      activityDao.getActivityById(activityId).map(_.id) must contain(activityId)

    }

    "get activities by ids" in {

      val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)

      activityDao.getActivitiesByIds(Seq(activityId)).map(_.id) must contain(activityId)

    }

    "replace activities" in {

      val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)
      val newActivityId = activityDao.save(makeActivityPrototype(), Seq(activityId))

      activityDao.getActivityById(activityId).flatMap(_.replacedBy) mustBe Some(newActivityId)

    }

    "find activities without tags" in {

      val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)
      activityRecipientDao.create(activityId, "someone", None)

      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)

    }

    "find activities with tags" in {

      val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)
      activityRecipientDao.create(activityId, "someone", None)
      activityTagDao.save(activityId, ActivityTag("name", TagValue("value")))

      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)

    }

  }

}
