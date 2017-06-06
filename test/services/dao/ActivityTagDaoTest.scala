package services.dao

import helpers.{BaseSpec, Fixtures, OneStartAppPerSuite}
import models.{ActivityTag, TagValue}

class ActivityTagDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val dao = get[ActivityTagDao]
  private val activityDao = get[ActivityDao]
  private val activitySave = Fixtures.activitySave.submissionDue

  val audienceId = "audience"

  "ActivityTagDao" should {

    "tag an activity then find it by the tag" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      dao.save(activityId, ActivityTag("name", None, TagValue("value", Some("Display value"))))

      val activities = dao.getActivitiesWithTags(Map("name" -> "value"), "tabula")

      activities must contain(activityId)
    }

    "return only activities with all tags" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)
      dao.save(activityId, ActivityTag("a", None, TagValue("a", None)))
      dao.save(activityId, ActivityTag("b", None, TagValue("b", None)))

      dao.getActivitiesWithTags(Map("a" -> "a", "b" -> "b"), "tabula") must contain(activityId)
      dao.getActivitiesWithTags(Map("a" -> "a", "c" -> "c"), "tabula") mustNot contain(activityId)
    }

  }

}
