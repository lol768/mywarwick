package services

import helpers.{Fixtures, OneStartAppPerSuite}
import models._
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}
import warwick.sso.Usercode

import scala.util.{Failure, Success}

class ActivityServiceTest extends PlaySpec with MockitoSugar {


  class Scope {
    val activityRecipientService = mock[ActivityRecipientService]
    val activityCreationDao = mock[ActivityCreationDao]
    val activityDao = mock[ActivityDao]
    val activityTagDao = mock[ActivityTagDao]
    val service = new ActivityServiceImpl(
      activityRecipientService,
      activityCreationDao,
      activityDao,
      activityTagDao,
      new PubSub {
        override def publish(topic: String, message: Any): Unit = {}
      },
      new MockDatabase()
    )

    val proto = Fixtures.activityPrototype.submissionDue
  }

  "ActivityServiceTest" should {

    "fail on no recipients" in new Scope {
      when(activityRecipientService.getRecipientUsercodes(Nil, Nil)) thenReturn Set[Usercode]()
      service.save(proto) must be (Failure(NoRecipientsException))
    }

    "save an item for each recipient" in new Scope {
      val createdActivity = Fixtures.activity.fromPrototype("1234", proto)
      val response = ActivityResponse(createdActivity, Nil)

      when(activityRecipientService.getRecipientUsercodes(Nil, Nil)) thenReturn Set(Usercode("cusebr"))
      when(activityTagDao.getActivitiesWithTags(Matchers.eq(Map()), Matchers.eq("tabula"))(any())) thenReturn Nil
      when(activityCreationDao.createActivity(Matchers.eq(proto), Matchers.eq(Set(Usercode("cusebr"))), Matchers.eq(Nil))(any())) thenReturn response

      service.save(proto) must be (Success("1234"))
    }

    // TODO test when there are activities to replace

  }
}
