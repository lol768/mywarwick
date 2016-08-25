package services

import helpers.{Fixtures, MockSchedulerService}
import models.{Audience, _}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.quartz.JobKey
import org.scalatest.LoneElement._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.ActivityError.{InvalidActivityType, InvalidTagName, InvalidTagValue, _}
import services.dao._
import services.job.PublishActivityJob
import warwick.sso.Usercode

class ActivityServiceTest extends PlaySpec with MockitoSugar {


  class Scope {
    val activityDao = mock[ActivityDao]
    val activityTypeService = mock[ActivityTypeService]
    val activityTagDao = mock[ActivityTagDao]
    val activityRecipientDao = mock[ActivityRecipientDao]
    val audienceDao = mock[AudienceDao]
    val scheduler = new MockSchedulerService()

    val service = new ActivityServiceImpl(
      new MockDatabase(),
      activityDao,
      activityTypeService,
      activityTagDao,
      audienceDao,
      activityRecipientDao,
      scheduler
    )

    when(activityTypeService.isValidActivityType(Matchers.any())).thenReturn(true)
    when(activityTypeService.isValidActivityTagName(Matchers.any())).thenReturn(true)
    when(activityTypeService.isValidActivityTag(Matchers.any(), Matchers.any())).thenReturn(true)

    val submissionDue = Fixtures.activitySave.submissionDue

    when(activityTagDao.getActivitiesWithTags(Matchers.eq(Map()), Matchers.eq("tabula"))(any())) thenReturn Nil
  }

  "ActivityServiceTest" should {

    "fail on empty audience" in new Scope {
      service.save(submissionDue, Audience.usercodes(Nil)) mustBe Left(Seq(EmptyAudience))
    }

    "save an item for each recipient" in new Scope {
      val recipients = Audience.usercode(Usercode("cusebr"))

      when(audienceDao.saveAudience(Matchers.eq(recipients))(any())).thenReturn("audience-id")
      when(activityDao.save(Matchers.eq(submissionDue), Matchers.eq("audience-id"), Matchers.eq(Seq.empty))(any())).thenReturn("activity-id")

      service.save(submissionDue, recipients) must be(Right("activity-id"))

      scheduler.triggeredJobs.map(_.getKey) must contain(new JobKey("activity-id", PublishActivityJob.name))
    }

    "fail with invalid activity type and tag name" in new Scope {
      when(activityTypeService.isValidActivityType(Matchers.any())).thenReturn(false)
      when(activityTypeService.isValidActivityTagName(Matchers.any())).thenReturn(false)

      val activity = submissionDue.copy(tags = Seq(ActivityTag("module", TagValue("CS118", Some("CS118 Programming for Computer Scientists")))))
      val result = service.save(activity, Audience.usercode(Usercode("custard")))

      result must be a 'left
      val e = result.left.get
      e must have length 2

      e.head mustBe an[InvalidActivityType]
      e.head must have('name ("due"))

      e(1) mustBe an[InvalidTagName]
      e(1) must have('name ("module"))
    }

    "fail with invalid activity tag value" in new Scope {
      when(activityTypeService.isValidActivityTag(Matchers.any(), Matchers.any())).thenReturn(false)

      val activity = submissionDue.copy(tags = Seq(ActivityTag("module", TagValue("CS118", Some("CS118 Programming for Computer Scientists")))))
      val result = service.save(activity, Audience.usercode(Usercode("custard")))

      result must be a 'left
      val e = result.left.get

      e.loneElement mustBe an[InvalidTagValue]
      e.loneElement must have('name ("module"), 'value ("CS118"))
    }

    "update - fail if activity does not exist" in new Scope {
      when(activityDao.getActivityById(Matchers.eq("activity"))(Matchers.any())).thenReturn(None)

      val result = service.update("activity", submissionDue, Audience.usercode(Usercode("custard")))

      result must be a 'left
      result.left.get must contain(DoesNotExist)
    }

    "update - fail if activity is already published" in new Scope {
      val existingActivity = Fixtures.activity.fromSave("activity", submissionDue).copy(publishedAt = DateTime.now.minusHours(1))
      when(activityDao.getActivityById(Matchers.eq("activity"))(Matchers.any())).thenReturn(Some(existingActivity))

      val result = service.update("activity", submissionDue, Audience.usercode(Usercode("custard")))

      result must be a 'left
      result.left.get must contain(AlreadyPublished)
    }

    "update an existing activity" in new Scope {
      val existingActivity = Fixtures.activity.fromSave("activity", submissionDue).copy(publishedAt = DateTime.now.plusHours(1))
      when(activityDao.getActivityById(Matchers.eq("activity"))(Matchers.any())).thenReturn(Some(existingActivity))

      val audience = Audience.usercode(Usercode("custard"))
      when(audienceDao.saveAudience(Matchers.eq(audience))(any())).thenReturn("audience")

      val result = service.update("activity", submissionDue, audience)

      result must be a 'right
      result.right.get must be("activity")

      verify(activityDao).update(Matchers.eq("activity"), Matchers.eq(submissionDue), Matchers.eq("audience"))(Matchers.any())
    }

    // TODO test when there are activities to replace

  }
}
