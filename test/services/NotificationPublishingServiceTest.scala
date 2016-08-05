package services

import helpers.{Fixtures, MockSchedulerService, OneStartAppPerSuite}
import models.news.Audience
import models.news.Audience.{DepartmentAudience, Staff}
import org.joda.time.DateTime
import org.quartz.JobKey
import org.scalatestplus.play.PlaySpec


class NotificationPublishingServiceTest extends PlaySpec with OneStartAppPerSuite {

  val notificationPublishingService = get[NotificationPublishingService]
  val activityService = get[ActivityService]
  val scheduler = get[SchedulerService].asInstanceOf[MockSchedulerService]

  val staffAudience = Audience(Seq(DepartmentAudience("IN", Seq(Staff))))
  val item = Fixtures.notificationSave.lunchtime

  "NotificationPublishingService" should {

    "save a notification and schedule it for publishing" in {
      scheduler.reset()

      val id = notificationPublishingService.publish(item.copy(publishDate = DateTime.now.plusHours(2)), staffAudience)

      val activity = activityService.getActivityById(id).get
      activity must have('title (item.text))

      val key = new JobKey(id, "PublishActivity")
      scheduler.deletedJobs must contain(key)
      scheduler.scheduledJobs.map(_.job.getKey) must contain(key)
    }

    "publish a notification now" in {
      scheduler.reset()

      val id = notificationPublishingService.publish(item, staffAudience)

      val activity = activityService.getActivityById(id).get
      activity must have('title (item.text))

      val key = new JobKey(id, "PublishActivity")
      scheduler.deletedJobs must contain(key)
      scheduler.triggeredJobs.map(_.getKey) must contain(key)
    }

  }

}
