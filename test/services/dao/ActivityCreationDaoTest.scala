package services.dao

import java.sql.Connection

import helpers.{Fixtures, OneStartAppPerSuite}
import models.{ActivityPrototype, ActivityTag, TagValue}
import org.scalatestplus.play.PlaySpec
import warwick.sso.Usercode

class ActivityCreationDaoTest extends PlaySpec with OneStartAppPerSuite {

  val activityCreationDao = app.injector.instanceOf[ActivityCreationDao]
  val activityDao = app.injector.instanceOf[ActivityDao]
  val activityTagDao = app.injector.instanceOf[ActivityTagDao]
  val activityRecipientDao = app.injector.instanceOf[ActivityRecipientDao]
  val activityPrototype = Fixtures.activityPrototype.submissionDue

  "ActivityCreationDao" should {

    def createActivity(activityPrototype: ActivityPrototype = activityPrototype)(implicit c: Connection): (String, String) = {
      val replacedActivityId = activityDao.save(activityPrototype, Seq.empty)

      val activityResponse = activityCreationDao.createActivity(
        activityPrototype.copy(
          tags = Seq[ActivityTag](
            ActivityTag("module", TagValue("CS118", None)),
            ActivityTag("assignment", TagValue("assignment-identifier", None))
          )
        ),
        recipients = Set(Usercode("someone")),
        replaces = Seq(replacedActivityId)
      )

      val activityId = activityResponse.activity.id

      (activityId,replacedActivityId)
    }

    "create an activity" in transaction { implicit c =>
      val (activityId, _) = createActivity()

      activityDao.getActivityById(activityId).map(_.id) mustBe Some(activityId)
    }

    "create an activity with null text" in transaction { implicit c =>
      val (activityId, _) = createActivity(activityPrototype.copy(text = None))

      activityDao.getActivityById(activityId).map(_.text) mustBe Some(None)
    }

    "replace a previous activity" in transaction { implicit c =>
      val (activityId, replacedActivityId) = createActivity()

      activityDao.getActivityById(replacedActivityId).flatMap(_.replacedBy) mustBe Some(activityId)
    }

    "associate the activity with its recipients" in transaction { implicit c =>
      val (activityId, _) = createActivity()

      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)
    }

    "tag the activity with the specified tags" in transaction { implicit c =>
      val (activityId, _) = createActivity()

      activityTagDao.getActivitiesWithTags(Map("module" -> "CS118"), "tabula") must contain(activityId)
      activityTagDao.getActivitiesWithTags(Map("assignment" -> "assignment-identifier"), "tabula") must contain(activityId)
    }

  }

}
