package services.dao

import anorm.SQL
import anorm.SqlParser._
import helpers.{Fixtures, OneStartAppPerSuite}
import models.Audience
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import warwick.anorm.converters.ColumnConversions._

class ActivityRecipientDaoTest extends PlaySpec with OneStartAppPerSuite {

  val activityDao = get[ActivityDao]
  val activityRecipientDao = get[ActivityRecipientDao]
  
  val activitySave = Fixtures.activitySave.submissionDue

  val audienceId = "audience"

  "ActivityRecipientDao" should {

    "create a recipient" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)

      activityRecipientDao.create(activityId, "someone", None)

      val count = SQL("SELECT COUNT(*) FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = {activityId} AND USERCODE = 'someone'")
        .on('activityId -> activityId)
        .as(scalar[Int].single)

      count must be(1)
    }

    "mark an activity as sent" in transaction { implicit c =>
      val activityId = activityDao.save(activitySave, audienceId, Seq.empty)

      activityRecipientDao.create(activityId, "someone", None)
      activityRecipientDao.markSent(activityId, "someone")

      val date = SQL("SELECT SENT_AT FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = {activityId} AND USERCODE = 'someone'")
        .on('activityId -> activityId)
        .as(scalar[DateTime].singleOpt)

      date must not be None
    }

  }

}
