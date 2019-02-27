package services

import helpers.{BaseSpec, Fixtures, MockSchedulerService}
import models.{Audience, _}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{eq => isEq, _}
import org.mockito.Mockito._
import org.quartz.JobKey
import org.scalatest.LoneElement._
import org.scalatest.mockito.MockitoSugar
import services.ActivityError.{InvalidTagValue, _}
import services.dao._
import services.job.PublishActivityJob
import warwick.sso.{UserLookupService, Usercode}

import scala.util.Try

class ActivityServiceTest extends BaseSpec with MockitoSugar {

  class Scope {
    val activityDao: ActivityDao = mock[ActivityDao]
    val activityTypeService: ActivityTypeService = mock[ActivityTypeService]
    val activityTagDao: ActivityTagDao = mock[ActivityTagDao]
    val activityRecipientDao: ActivityRecipientDao = mock[ActivityRecipientDao]
    val audienceService: AudienceService = mock[AudienceService]
    val audienceDao: AudienceDao = mock[AudienceDao]
    val activityMuteDao: ActivityMuteDao = mock[ActivityMuteDao]
    val userLookupService: UserLookupService = mock[UserLookupService]
    val scheduler = new MockSchedulerService()

    val service = new ActivityServiceImpl(
      new MockDatabase(),
      activityDao,
      activityTypeService,
      activityTagDao,
      audienceService,
      audienceDao,
      activityRecipientDao,
      activityMuteDao,
      scheduler,
      userLookupService
    )

    when(activityTypeService.isValidActivityType(any())).thenReturn(true)
    when(activityTypeService.isValidActivityTagName(any())).thenReturn(true)
    when(activityTypeService.isValidActivityTag(any(), any())).thenReturn(true)

    val submissionDue: ActivitySave = Fixtures.activitySave.submissionDue

    when(activityTagDao.getActivitiesWithTags(isEq(Map()), isEq("tabula"))(any())) thenReturn Nil
  }

  "ActivityServiceTest" should {

    "fail on empty audience" in new Scope {
      service.save(submissionDue, Audience.usercodes(Nil)) mustBe Left(Seq(EmptyAudience))
    }

    "save an item for each recipient" in new Scope {
      private val recipients = Audience.usercode(Usercode("cusebr"))

      when(audienceDao.saveAudience(isEq(recipients))(any())).thenReturn("audience-id")
      when(activityDao.save(isEq(submissionDue), isEq("audience-id"), isEq(AudienceSize.Finite(1)), isEq(Seq.empty))(any())).thenReturn("activity-id")
      when(audienceService.resolve(recipients)).thenReturn(Try(Set(Usercode("cusebr"))))

      service.save(submissionDue, recipients) must be(Right("activity-id"))

      scheduler.triggeredJobs.map(_.getKey) must contain(new JobKey("activity-id", PublishActivityJob.name))
    }

    "not fail with invalid activity type" in new Scope {
      private val recipients = Audience.usercode(Usercode("custard"))
      when(activityTypeService.isValidActivityType(any())).thenReturn(false)
      when(audienceDao.saveAudience(any())(any())).thenReturn("audience-id")
      when(activityDao.save(isEq(submissionDue), isEq("audience-id"), isEq(AudienceSize.Finite(1)), isEq(Seq.empty))(any())).thenReturn("activity-id")
      when(audienceService.resolve(recipients)).thenReturn(Try(Set(Usercode("custard"))))

      private val activity = submissionDue
      private val result = service.save(activity, recipients)

      result must be a 'right
    }

    "not fail with invalid tag name" in new Scope {
      private val recipients = Audience.usercode(Usercode("custard"))
      when(activityTypeService.isValidActivityTagName(any())).thenReturn(false)
      when(audienceDao.saveAudience(any())(any())).thenReturn("audience-id")
      when(activityDao.save(isEq(submissionDue), isEq("audience-id"), isEq(AudienceSize.Finite(1)), isEq(Seq.empty))(any())).thenReturn("activity-id")
      when(audienceService.resolve(recipients)).thenReturn(Try(Set(Usercode("custard"))))

      private val activity = submissionDue
      private val result = service.save(activity, recipients)

      result must be a 'right
    }

    "fail with valid tab name but invalid tag value" in new Scope {
      when(activityTypeService.isValidActivityTagName(any())).thenReturn(true)
      when(activityTypeService.isValidActivityTag(any(),any())).thenReturn(false)
      when(audienceDao.saveAudience(any())(any())).thenReturn("audience-id")
      when(activityDao.save(isEq(submissionDue), isEq("audience-id"), isEq(AudienceSize.Finite(1)), isEq(Seq.empty))(any())).thenReturn("activity-id")

      private val activity = submissionDue.copy(tags = Seq(ActivityTag("module", None, TagValue("CS118", Some("CS118 Programming for Computer Scientists")))))
      private val result = service.save(activity, Audience.usercode(Usercode("custard")))

      result must be a 'left
      private val e = result.left.get

      e.loneElement mustBe an[InvalidTagValue]
    }

    "fail with invalid activity tag value" in new Scope {
      when(activityTypeService.isValidActivityTag(any(), any())).thenReturn(false)

      private val activity = submissionDue.copy(tags = Seq(ActivityTag("module", None, TagValue("CS118", Some("CS118 Programming for Computer Scientists")))))
      private val result = service.save(activity, Audience.usercode(Usercode("custard")))

      result must be a 'left
      private val e = result.left.get

      e.loneElement mustBe an[InvalidTagValue]
      e.loneElement must have('name ("module"), 'value ("CS118"))
    }

    "update - fail if activity does not exist" in new Scope {
      when(activityDao.getActivityById(isEq("activity"))(any())).thenReturn(None)

      private val result = service.update("activity", submissionDue, Audience.usercode(Usercode("custard")))

      result must be a 'left
      result.left.get must contain(DoesNotExist)
    }

    "update - fail if activity is already published" in new Scope {
      private val existingActivity = Fixtures.activity.fromSave("activity", submissionDue).copy(publishedAt = DateTime.now.minusHours(1))
      when(activityDao.getActivityById(isEq("activity"))(any())).thenReturn(Some(existingActivity))

      private val result = service.update("activity", submissionDue, Audience.usercode(Usercode("custard")))

      result must be a 'left
      result.left.get must contain(AlreadyPublished)
    }

    "update an existing activity" in new Scope {
      private val existingActivity = Fixtures.activity.fromSave("activity", submissionDue).copy(publishedAt = DateTime.now.plusHours(1))
      when(activityDao.getActivityById(isEq("activity"))(any())).thenReturn(Some(existingActivity))

      private val audience = Audience.usercode(Usercode("custard"))
      when(audienceDao.saveAudience(isEq(audience))(any())).thenReturn("audience")
      when(audienceService.resolve(audience)).thenReturn(Try(Set(Usercode("custard"))))

      private val result = service.update("activity", submissionDue, audience)

      result must be a 'right
      result.right.get must be("activity")

      verify(activityDao).update(isEq("activity"), isEq(submissionDue), isEq("audience"), isEq(AudienceSize.Finite(1)))(any())
    }

    "get mutes" in {
      val activityMute = ActivityMute(
        usercode = null,
        createdAt = null,
        expiresAt = None,
        activityType = None,
        providerId = None,
        tags = Nil
      )
      // Null expires
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(activityMute))
        private val result = service.getActivityMutes(activity, Nil, Set.empty)
        result must have length 1
      }
      // Expires in future
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        private val now = DateTime.now
        private val thisMute = activityMute.copy(expiresAt = Some(now.plusDays(1)))
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(thisMute))
        private val result = service.getActivityMutes(activity, Nil, Set.empty, now)
        result must have length 1
      }
      // Expires in past
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        private val now = DateTime.now
        private val thisMute = activityMute.copy(expiresAt = Some(now.minusDays(1)))
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(thisMute))
        private val result = service.getActivityMutes(activity, Nil, Set.empty, now)
        result must have length 0
      }

      val tag1 = ActivityTag("tag1", None, TagValue("value1"))
      val tag2 = ActivityTag("tag2", None, TagValue("value1"))
      val tag3 = ActivityTag("tag2", None, TagValue("value2"))
      // Empty mute tags
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(activityMute))
        private val result = service.getActivityMutes(activity, Seq(tag1), Set.empty)
        result must have length 1
      }
      // Empty activity tags
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        private val thisMute = activityMute.copy(tags = Seq(tag1))
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(thisMute))
        private val result = service.getActivityMutes(activity, Nil, Set.empty)
        result must have length 1
      }
      // Incorrect tag name
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        private val thisMute = activityMute.copy(tags = Seq(tag1))
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(thisMute))
        private val result = service.getActivityMutes(activity, Seq(tag2), Set.empty)
        result must have length 0
      }
      // Incorrect tag value
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        private val thisMute = activityMute.copy(tags = Seq(tag2))
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(thisMute))
        private val result = service.getActivityMutes(activity, Seq(tag3), Set.empty)
        result must have length 0
      }
      // Tag match single
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        private val thisMute = activityMute.copy(tags = Seq(tag3))
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(thisMute))
        private val result = service.getActivityMutes(activity, Seq(tag3), Set.empty)
        result must have length 1
      }
      // Tag match collection
      new Scope {
        private val activity = Fixtures.activity.fromSave("activity", submissionDue)
        private val thisMute = activityMute.copy(tags = Seq(tag1, tag2))
        when(activityMuteDao.mutesForActivity(isEq(activity), any[Set[Usercode]])(any())).thenReturn(Seq(thisMute))
        private val result = service.getActivityMutes(activity, Seq(tag1, tag2, tag3), Set.empty)
        result must have length 1
      }
    }

    "require option when saving mute" in {
      new Scope {
        private val mute = ActivityMuteSave(
          Usercode("cusfal"),
          None,
          None,
          None,
          Nil
        )
        private val result = service.save(mute)
        result.isLeft must be (true)
        result.left.get must have size 1
        (result.left.get.head match {
          case MuteNoOptions => true
          case _ => false
        }) must be (true)
      }
    }
  }
}
