package services.dao

import helpers.OneStartAppPerSuite
import helpers.TestObjectFactory._
import models.{ActivityTag, TagValue}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
import warwick.sso.Usercode

class ActivityCreationDaoTest extends PlaySpec with OneStartAppPerSuite {

  implicit val c = app.injector.instanceOf[Database].getConnection()

  val activityCreationDao = app.injector.instanceOf[ActivityCreationDao]
  val activityDao = app.injector.instanceOf[ActivityDao]
  val activityTagDao = app.injector.instanceOf[ActivityTagDao]
  val activityRecipientDao = app.injector.instanceOf[ActivityRecipientDao]

  "ActivityCreationDao" should {

    val replacedActivityId = activityDao.save(makeActivityPrototype(), Seq.empty)

    val activityResponse = activityCreationDao.createActivity(
      makeActivityPrototype().copy(
        tags = Seq[ActivityTag](
          ActivityTag("module", TagValue("CS118", None)),
          ActivityTag("assignment", TagValue("assignment-identifier", None))
        )
      ),
      recipients = Set(Usercode("someone")),
      replaces = Seq(replacedActivityId)
    )

    val activityId = activityResponse.activity.id

    "create an activity" in {
      activityDao.getActivityById(activityId).map(_.id) mustBe Some(activityId)
    }

    "replace a previous activity" in {
      activityDao.getActivityById(replacedActivityId).flatMap(_.replacedBy) mustBe Some(activityId)
    }

    "associate the activity with its recipients" in {
      activityDao.getActivitiesForUser("someone", 100).map(_.activity.id) must contain(activityId)
    }

    "tag the activity with the specified tags" in {
      activityTagDao.getActivitiesWithTags(Map("module" -> "CS118"), "tabula") must contain(activityId)
      activityTagDao.getActivitiesWithTags(Map("assignment" -> "assignment-identifier"), "tabula") must contain(activityId)
    }

  }

}
