package services

import helpers.{Fixtures, MockSchedulerService, OneStartAppPerSuite}
import models.news.Audience
import models.news.Audience.{DepartmentAudience, Staff}
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

      val id = notificationPublishingService.publish(item, staffAudience)

      val render = activityService.getActivityById(id).get

      render must have('title (item.text))

      scheduler.scheduledJobs.map(_.job.getKey) must contain(new JobKey(id, "PublishActivity"))
    }

  }

}
