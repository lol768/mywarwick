package services

import helpers.Fixtures
import models._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeApplication
import services.dao.{ActivityTagDao, ActivityDao, ActivityCreationDao}
import warwick.sso.Usercode

import scala.util.{Success, Failure}


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
      FakeApplication(),
      new PubSub {
        override def publish(topic: String, message: Any): Unit = {}
      }
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
      when(activityTagDao.getActivitiesWithTags(Map(), "tabula")) thenReturn Nil
      when(activityCreationDao.createActivity(proto, Set(Usercode("cusebr")), Nil)) thenReturn response

      service.save(proto) must be (Success("1234"))
    }

    // TODO test when there are activities to replace

  }
}
