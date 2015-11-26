package services

import anorm.SQL
import anorm.SqlParser._
import helpers.OneStartAppPerSuite
import helpers.TestObjectFactory._
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
import services.dao.{ActivityDao, ActivityRecipientDao}
import warwick.anorm.converters.ColumnConversions._

class ActivityRecipientDaoTest extends PlaySpec with OneStartAppPerSuite {

  val db = app.injector.instanceOf[Database]
  implicit val c = db.getConnection()

  val activityDao = app.injector.instanceOf[ActivityDao]
  val activityRecipientDao = app.injector.instanceOf[ActivityRecipientDao]

  val activityId = activityDao.save(makeActivityPrototype(), Seq.empty)

  "ActivityRecipientDao" should {

    "create a recipient" in {

      activityRecipientDao.create(activityId, "someone", None)

      val count = SQL("SELECT COUNT(*) FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = {activityId} AND USERCODE = 'someone'").on('activityId -> activityId).as(scalar[Int].single)

      count must be(1)

    }

    "mark an activity as sent" in {

      activityRecipientDao.markSent(activityId, "someone")

      val date = SQL("SELECT SENT_AT FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = {activityId} AND USERCODE = 'someone'").on('activityId -> activityId).as(scalar[DateTime].singleOpt)

      date must not be None

    }

  }

}
