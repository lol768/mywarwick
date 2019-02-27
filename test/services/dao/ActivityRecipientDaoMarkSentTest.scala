package services.dao

import anorm._
import anorm.SqlParser._
import helpers.{BaseSpec, Fixtures, OneStartAppPerSuite}
import models.{ActivitySave, AudienceSize}
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Testing concurrency of markProcessed. This is in its own test suite because it
  * commits transactions so should use a fresh app + DB.
  */
class ActivityRecipientDaoMarkSentTest extends BaseSpec with OneStartAppPerSuite with ScalaFutures {

  val activityDao: ActivityDao = get[ActivityDao]
  val activityRecipientDao: ActivityRecipientDao = get[ActivityRecipientDao]
  
  val activitySave: ActivitySave = Fixtures.activitySave.submissionDue
  val audienceId = "audience"

  "ActivityRecipientDao" should {

    "mark activity as sent concurrency" in {
      import scala.concurrent.ExecutionContext.Implicits.global

      val COUNT = 10
      val usercodes: Seq[String] = (1 to COUNT).map(i => s"user$i")
      usercodes must have length COUNT.toLong

      // create the activity recipients
      val activityId = transaction(rollback=false) { implicit c =>
        val id = activityDao.save(activitySave, audienceId, AudienceSize.Finite(COUNT), Nil)
        usercodes.foreach { usercode =>
          activityRecipientDao.create(id, usercode, None, shouldNotify = false)
        }
        id
      }

      val markedSent = for {
        usercode <- usercodes
        _ <- 1 to 5
      } yield Future {
        transaction(rollback=false) { implicit c =>
          activityRecipientDao.markProcessed(activityId, usercode)
        }
      }

      Await.result(Future.sequence(markedSent), 5.seconds)

      transaction { implicit c =>
        val count = SQL"SELECT SENT_COUNT FROM ACTIVITY WHERE ID = $activityId"
          .as(scalar[Int].single)

        count mustBe COUNT
      }
    }

  }

}
