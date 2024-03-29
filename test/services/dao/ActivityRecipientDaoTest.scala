package services.dao

import anorm._
import anorm.SqlParser._
import helpers.{BaseSpec, Fixtures, OneStartAppPerSuite}
import models.{ActivitySave, AudienceSize}
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._

class ActivityRecipientDaoTest extends BaseSpec with OneStartAppPerSuite {

  val activityDao: ActivityDao = get[ActivityDao]
  val activityRecipientDao: ActivityRecipientDao = get[ActivityRecipientDao]
  
  val activitySave: ActivitySave = Fixtures.activitySave.submissionDue

  val audienceId = "audience"

  "ActivityRecipientDao" should {

    "create a recipient" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)

      activityRecipientDao.create(activityId, "someone", None, shouldNotify = false)

      val count = SQL"SELECT COUNT(*) FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = $activityId AND USERCODE = 'someone'"
        .as(scalar[Int].single)

      count mustBe 1
    }

    "mark an activity as sent" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, AudienceSize.Public, Seq.empty)

      activityRecipientDao.create(activityId, "someone", None, shouldNotify = false)
      activityRecipientDao.markProcessed(activityId, "someone")

      val date = SQL"SELECT SENT_AT FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = $activityId AND USERCODE = 'someone'"
        .as(scalar[DateTime].singleOpt)

      date must not be None

      val count = SQL"SELECT SENT_COUNT FROM ACTIVITY WHERE ID = $activityId"
        .as(scalar[Int].single)

      count mustBe 1
    }

  }

}
