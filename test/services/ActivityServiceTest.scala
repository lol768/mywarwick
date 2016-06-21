package services

import actors.WebsocketActor.Notification
import helpers.Fixtures
import models._
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}
import services.messaging.MessagingService
import warwick.sso.Usercode

import scala.util.{Failure, Success}

class ActivityServiceTest extends PlaySpec with MockitoSugar {


  class Scope {
    val activityRecipientService = mock[ActivityRecipientService]
    val activityCreationDao = mock[ActivityCreationDao]
    val activityDao = mock[ActivityDao]
    val activityTagDao = mock[ActivityTagDao]
    val messaging = mock[MessagingService]
    val pubSub = mock[PubSub]
    val service = new ActivityServiceImpl(
      activityDao,
      activityCreationDao,
      activityTagDao,
      messaging,
      pubSub,
      new MockDatabase()
    )

    val submissionDue = Fixtures.activitySave.submissionDue

    when(activityTagDao.getActivitiesWithTags(Matchers.eq(Map()), Matchers.eq("tabula"))(any())) thenReturn Nil
  }

  "ActivityServiceTest" should {

    "fail on no recipients" in new Scope {
      service.save(submissionDue, Set.empty[Usercode]) must be (Failure(NoRecipientsException))
    }

    "save an item for each recipient" in new Scope {
      val createdActivity = Fixtures.activity.fromSave("1234", submissionDue)
      val response = ActivityResponse(createdActivity, None, Nil)
      val recipients = Set(Usercode("cusebr"))

      when(activityRecipientService.getRecipientUsercodes(Nil, Nil)) thenReturn recipients
      when(activityCreationDao.createActivity(Matchers.eq(submissionDue), Matchers.eq(Set(Usercode("cusebr"))), Matchers.eq(Nil))(any())) thenReturn response

      service.save(submissionDue, recipients) must be (Success("1234"))

      verify(messaging).send(recipients, createdActivity)
      verify(pubSub).publish("cusebr", Notification(response))
    }

    "not notify when shouldNotify is false" in new Scope {
      val createdActivity = Fixtures.activity.fromSave("1234", submissionDue.copy(shouldNotify = false))
      val response = ActivityResponse(createdActivity, None, Nil)
      val recipients = Set(Usercode("cusebr"))

      when(activityRecipientService.getRecipientUsercodes(Nil, Nil)) thenReturn recipients
      when(activityCreationDao.createActivity(Matchers.eq(submissionDue), Matchers.eq(Set(Usercode("cusebr"))), Matchers.eq(Nil))(any())) thenReturn response

      service.save(submissionDue, recipients) must be (Success("1234"))

      verifyZeroInteractions(messaging)
      verify(pubSub).publish("cusebr", Notification(response))
    }

    // TODO test when there are activities to replace

  }
}
