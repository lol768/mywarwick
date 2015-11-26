package services

import anorm.SQL
import anorm.SqlParser._
import helpers.OneStartAppPerSuite
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
import services.dao.ActivityRecipientDaoImpl
import warwick.anorm.converters.ColumnConversions._

class ActivityRecipientDaoTest extends PlaySpec with OneStartAppPerSuite {

  val db = app.injector.instanceOf[Database]
  implicit val c = db.getConnection()

  val activityRecipientDao = new ActivityRecipientDaoImpl(db)

  SQL(
    """
       INSERT INTO ACTIVITY (ID, PROVIDER_ID, TYPE, TITLE, TEXT, GENERATED_AT, CREATED_AT, SHOULD_NOTIFY)
       VALUES ('abc', 'tabula', 'due', 'Coursework due', 'Your coursework is due in 7 days', SYSDATE, SYSDATE, 1)
    """).execute()

  SQL(
    """
         INSERT INTO ACTIVITY_RECIPIENT (ACTIVITY_ID, USERCODE, CREATED_AT, GENERATED_AT) VALUES
         ('abc', 'someone', SYSDATE, SYSDATE)
    """).execute()
  implicit val c = db.getConnection()
  "ActivityRecipientDao" should {

    "create a recipient" in {

      activityRecipientDao.create("abc", "other", None)

      val count = SQL("SELECT COUNT(*) FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = 'abc' AND USERCODE = 'other'").as(scalar[Int].single)

      count must be(1)

    }

    "mark an activity as sent" in {

      activityRecipientDao.markSent("abc", "someone")

      val date = SQL("SELECT SENT_AT FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = 'abc' AND USERCODE = 'someone'").as(scalar[DateTime].singleOpt)

      date must not be None

    }

  }

}
