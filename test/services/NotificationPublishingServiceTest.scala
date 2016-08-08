package services

import helpers.{Fixtures, MockSchedulerService, OneStartAppPerSuite}
import models.news.Audience
import models.news.Audience.{DepartmentAudience, Staff}
import org.joda.time.DateTime
import org.quartz.JobKey
import org.scalatestplus.play.PlaySpec
import services.ActivityError.AlreadyPublished


class NotificationPublishingServiceTest extends PlaySpec with OneStartAppPerSuite {

  val notificationPublishingService = get[NotificationPublishingService]
  val activityService = get[ActivityService]
  val scheduler = get[SchedulerService].asInstanceOf[MockSchedulerService]

  val staffAudience = Audience(Seq(DepartmentAudience("IN", Seq(Staff))))
  val item = Fixtures.notificationSave.lunchtime

  "NotificationPublishingService" should {

    "save a notification and schedule it for publishing" in {
      scheduler.reset()

      val id = notificationPublishingService.publish(item.copy(publishDate = DateTime.now.plusHours(2)), staffAudience).right.get

      val activity = activityService.getActivityById(id).get
      activity must have('title (item.text))

      val key = new JobKey(id, "PublishActivity")
      scheduler.deletedJobs must contain(key)
      scheduler.scheduledJobs.map(_.job.getKey) must contain(key)
    }

    "publish a notification now" in {
      scheduler.reset()

      val id = notificationPublishingService.publish(item, staffAudience).right.get

      val activity = activityService.getActivityById(id).get
      activity must have('title (item.text))

      val key = new JobKey(id, "PublishActivity")
      scheduler.deletedJobs must contain(key)
      scheduler.triggeredJobs.map(_.getKey) must contain(key)
    }

    "update a notification" in {
      val id = notificationPublishingService.publish(item.copy(publishDate = DateTime.now.plusHours(2)), staffAudience).right.get

      scheduler.reset()

      val result = notificationPublishingService.update(id, item.copy(publishDate = DateTime.now.plusHours(3)), staffAudience)

      result must be('right)

      val key = new JobKey(id, "PublishActivity")
      scheduler.deletedJobs must contain(key)
      scheduler.scheduledJobs.map(_.job.getKey) must contain(key)
    }

    "not update a published notification" in {
      val id = notificationPublishingService.publish(item.copy(publishDate = DateTime.now.minusDays(1)), staffAudience).right.get

      scheduler.reset()

      val result = notificationPublishingService.update(id, item, staffAudience)

      result must be('left)
      result.left.get must contain(AlreadyPublished)

      scheduler.deletedJobs must be(empty)
      scheduler.triggeredJobs must be(empty)
    }

    "delete a notification" in {
      val id = notificationPublishingService.publish(item.copy(publishDate = DateTime.now.plusDays(1)), staffAudience).right.get

      val result = notificationPublishingService.delete(id)

      result must be('right)
    }

    "not delete a published notification" in {
      val id = notificationPublishingService.publish(item.copy(publishDate = DateTime.now.minusDays(1)), staffAudience).right.get

      val result = notificationPublishingService.delete(id)

      result must be('left)
      result.left.get must contain(AlreadyPublished)
    }

  }

}
