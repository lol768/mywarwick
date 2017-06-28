package services

import helpers.{BaseSpec, Fixtures}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import services.dao._
import services.messaging.{MessagingServiceImpl, OutputService}
import warwick.sso.{UserLookupService, Usercode}

class MessagingServiceTest extends BaseSpec with MockitoSugar {

  class Scope {
    val activityService: ActivityService = mock[ActivityService]
    val activityServiceProvider = new javax.inject.Provider[ActivityService] {
      override def get(): ActivityService = activityService
    }
    val userLookupService: UserLookupService = mock[UserLookupService]
    val emailer: OutputService = mock[OutputService]
    val mobile: OutputService = mock[OutputService]
    val messagingDao: MessagingDao = mock[MessagingDao]
    val emailPrefService: EmailNotificationsPrefService = mock[EmailNotificationsPrefService]

    val service = new MessagingServiceImpl(
      new MockDatabase(),
      activityServiceProvider,
      userLookupService,
      emailPrefService,
      emailer,
      mobile,
      messagingDao
    )
  }

  "MessagingServiceTest" should {

    "mute recipients" in new Scope {
      private val activity = getTestingActivity
      private val activityRender = getTestingRenderFromActivity(activity)

      private val recipients = Set(Usercode("cusebr"), Usercode("cusfal"))
      when(activityService.getActivityRenderById(activity.id)).thenReturn(Some(activityRender))
      when(activityService.getActivityMutes(activityRender.activity, activityRender.tags, recipients)).thenReturn(Seq(
        ActivityMute(
          usercode = Usercode("cusfal"),
          createdAt = null,
          expiresAt = None,
          activityType = None,
          providerId = None,
          tags = Nil
        )
      ))

      service.send(recipients, activity)

      verify(messagingDao, times(1)).save(Matchers.eq(activity), Matchers.eq(Usercode("cusebr")), Matchers.eq(Output.Mobile))(Matchers.any())
      verify(messagingDao, times(0)).save(Matchers.eq(activity), Matchers.eq(Usercode("cusfal")), Matchers.eq(Output.Mobile))(Matchers.any())
    }

    "doesn't send emails when the user is opted-out" in new Scope {
      private val activity = getTestingActivity
      private val activityRender = getTestingRenderFromActivity(activity)

      when(activityService.getActivityRenderById(activity.id)).thenReturn(Some(activityRender))
      when(activityService.getProvider(activity.providerId)).thenReturn(Some(activityRender.provider))
      private val testUser = Usercode("u1673477")
      private val recipients = Set(testUser)
      when(activityService.getActivityMutes(activityRender.activity, activityRender.tags, recipients)).thenReturn(Nil)
      when(emailPrefService.get(testUser)).thenReturn(false)
      service.send(recipients, activity)
      verify(messagingDao, never()).save(Matchers.eq(activity), Matchers.eq(testUser), Matchers.eq(Output.Email))(Matchers.any())
    }

    "doesn't send emails when the user is opted-in but the activity isn't" in new Scope {
      private val activity = getTestingActivity.copy(sendEmail = Some(false))
      private val activityRender = getTestingRenderFromActivity(activity)

      when(activityService.getActivityRenderById(activity.id)).thenReturn(Some(activityRender))
      private val testUser = Usercode("u1673477")
      private val recipients = Set(testUser)
      when(activityService.getActivityMutes(activityRender.activity, activityRender.tags, recipients)).thenReturn(Nil)
      when(emailPrefService.get(testUser)).thenReturn(true)
      service.send(recipients, activity)
      verify(messagingDao, never()).save(Matchers.eq(activity), Matchers.eq(testUser), Matchers.eq(Output.Email))(Matchers.any())
      verify(activityService, never()).getProvider(activity.providerId)
    }

    "doesn't send emails when the user is opted-in and the provider isn't" in new Scope {
      private val activity = getTestingActivity
      private val activityRender = getTestingRenderFromActivity(activity)

      when(activityService.getActivityRenderById(activity.id)).thenReturn(Some(activityRender))
      when(activityService.getProvider(activity.providerId)).thenReturn(Some(activityRender.provider.copy(sendEmail = false)))
      private val testUser = Usercode("u1673477")
      private val recipients = Set(testUser)
      when(activityService.getActivityMutes(activityRender.activity, activityRender.tags, recipients)).thenReturn(Nil)
      when(emailPrefService.get(testUser)).thenReturn(true)
      service.send(recipients, activity)
      verify(messagingDao, never()).save(Matchers.eq(activity), Matchers.eq(testUser), Matchers.eq(Output.Email))(Matchers.any())
    }

    "send emails when the user is opted-in and the activity is" in new Scope {
      private val activity = getTestingActivity.copy(sendEmail = Some(true))
      private val activityRender = getTestingRenderFromActivity(activity)

      when(activityService.getActivityRenderById(activity.id)).thenReturn(Some(activityRender))
      private val testUser = Usercode("u1673477")
      private val recipients = Set(testUser)
      when(activityService.getActivityMutes(activityRender.activity, activityRender.tags, recipients)).thenReturn(Nil)
      when(emailPrefService.get(testUser)).thenReturn(true)
      service.send(recipients, activity)
      verify(messagingDao, times(1)).save(Matchers.eq(activity), Matchers.eq(testUser), Matchers.eq(Output.Email))(Matchers.any())
      verify(activityService, never()).getProvider(activity.providerId)
    }

    "send emails when the user is opted-in and the activity is undefined and the provider is" in new Scope {
      private val activity = getTestingActivity
      private val activityRender = getTestingRenderFromActivity(activity)

      when(activityService.getActivityRenderById(activity.id)).thenReturn(Some(activityRender))
      when(activityService.getProvider(activity.providerId)).thenReturn(Some(activityRender.provider))
      private val testUser = Usercode("u1673477")
      private val recipients = Set(testUser)
      when(activityService.getActivityMutes(activityRender.activity, activityRender.tags, recipients)).thenReturn(Nil)
      when(emailPrefService.get(testUser)).thenReturn(true)
      service.send(recipients, activity)
      verify(messagingDao, times(1)).save(Matchers.eq(activity), Matchers.eq(testUser), Matchers.eq(Output.Email))(Matchers.any())
    }

  }

  private def getTestingActivity = Fixtures.activity.fromSave("123", Fixtures.activitySave.submissionDue)

  private def getTestingRenderFromActivity(activity: Activity, providerSendEmail: Boolean = true) = ActivityRender(
    activity = activity,
    icon = None,
    tags = Nil,
    provider = ActivityProvider(activity.providerId, providerSendEmail),
    `type` = ActivityType(activity.`type`)
  )
}
