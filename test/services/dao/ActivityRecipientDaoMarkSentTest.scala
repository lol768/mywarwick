package services.dao

import akka.actor.ActorSystem
import anorm._
import anorm.SqlParser._
import helpers.{BaseSpec, Fixtures, OneStartAppPerSuite}
import models.{ActivitySave, AudienceSize}
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

/**
  * Testing concurrency of markSent. This is in its own test suite because it
  * commits transactions so should use a fresh app + DB.
  */
class ActivityRecipientDaoMarkSentTest extends BaseSpec with OneStartAppPerSuite with ScalaFutures {

  val activityDao: ActivityDao = get[ActivityDao]
  val activityRecipientDao: ActivityRecipientDao = get[ActivityRecipientDao]

  val akka = get[ActorSystem]
  
  val activitySave: ActivitySave = Fixtures.activitySave.submissionDue
  val audienceId = "audience"

  "ActivityRecipientDao" should {

    "mark activity as sent concurrency" in {
      implicit val executor = akka.dispatcher

      val COUNT = 10
      val usercodes: Seq[String] = (1 to COUNT).map(i => s"user$i")
      usercodes must have length COUNT

      var activityId: String = null // yes

      transaction(rollback=false) { implicit c =>
        activityId = activityDao.save(activitySave, audienceId, AudienceSize.Finite(COUNT), Nil)
        usercodes.foreach { usercode =>
          activityRecipientDao.create(activityId, usercode, None, shouldNotify = false)
        }
      }

      val marked: Future[Unit] = Future.sequence(usercodes.flatMap { usercode =>
        for (i <- 1 to 5) yield Future {
          transaction(rollback=false) { implicit c =>
            activityRecipientDao.markSent(activityId, usercode)
          }
        }
      }).map(_ => Unit)

      marked.futureValue

      transaction { implicit c =>
        val count = SQL"SELECT SENT_COUNT FROM ACTIVITY WHERE ID = $activityId"
          .as(scalar[Int].single)

        count mustBe COUNT
      }
    }

  }

}
