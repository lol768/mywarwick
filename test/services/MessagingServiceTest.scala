package services

import helpers.{BaseSpec, Fixtures}
import models.{ActivityMute, ActivityRender, Output}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.scalatest.mockito.MockitoSugar
import services.dao._
import services.messaging.{MessagingServiceImpl, OutputService}
import warwick.sso.{UserLookupService, Usercode}
import org.mockito.Mockito._

class MessagingServiceTest extends BaseSpec with MockitoSugar {

  class Scope {
    val activityService = mock[ActivityService]
    val activityServiceProvider = new javax.inject.Provider[ActivityService] {
      override def get(): ActivityService = activityService
    }
    val userLookupService: UserLookupService = mock[UserLookupService]
    val emailer: OutputService = mock[OutputService]
    val mobile: OutputService = mock[OutputService]
    val messagingDao: MessagingDao = mock[MessagingDao]

    val service = new MessagingServiceImpl(
      new MockDatabase(),
      activityServiceProvider,
      userLookupService,
      emailer,
      mobile,
      messagingDao
    )
  }

  "MessagingServiceTest" should {

    "mute recipients" in new Scope {
      private val activity = Fixtures.activity.fromSave("123", Fixtures.activitySave.submissionDue)
      private val activityRender = ActivityRender(
        activity = activity,
        icon = None,
        tags = Nil
      )
      when(activityService.getActivityRenderById(activity.id)).thenReturn(Some(activityRender))
      when(activityService.getActivityMutes(activityRender.activity, activityRender.tags)).thenReturn(Seq(
        ActivityMute(
          usercode = Usercode("cusfal"),
          createdAt = null,
          expiresAt = None,
          activityType = None,
          providerId = None,
          tags = Nil
        )
      ))

      service.send(
        Set(Usercode("cusebr"), Usercode("cusfal")),
        activity
      )

      verify(messagingDao, times(1)).save(Matchers.eq(activity), Matchers.eq(Usercode("cusebr")), Matchers.eq(Output.Mobile))(Matchers.any())
      verify(messagingDao, times(0)).save(Matchers.eq(activity), Matchers.eq(Usercode("cusfal")), Matchers.eq(Output.Mobile))(Matchers.any())
    }

  }

}
