package services.dao

import helpers.OneStartAppPerSuite
import helpers.TestObjectFactory._
import models.{ActivityTag, TagValue}
import org.scalatestplus.play.PlaySpec
import play.api.db.Database

class ActivityTagDaoTest extends PlaySpec with OneStartAppPerSuite {

  val db = app.injector.instanceOf[Database]
  implicit val c = db.getConnection()

  val activityDao = app.injector.instanceOf[ActivityDao]
  val dao = app.injector.instanceOf[ActivityTagDao]

  "ActivityTagDao" should {

    "tag an activity then find it by the tag" in {

      val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)
      dao.save(activityId, ActivityTag("name", TagValue("value", Some("Display value"))))

      val activities = dao.getActivitiesWithTags(Map("name" -> "value"), "tabula")

      activities must contain(activityId)

    }

    "return only activities with all tags" in {

      val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)
      dao.save(activityId, ActivityTag("a", TagValue("a", None)))
      dao.save(activityId, ActivityTag("b", TagValue("b", None)))

      dao.getActivitiesWithTags(Map("a" -> "a", "b" -> "b"), "tabula") must contain(activityId)
      dao.getActivitiesWithTags(Map("a" -> "a", "c" -> "c"), "tabula") mustNot contain(activityId)

    }

  }

}
