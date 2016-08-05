package services

import helpers.{Fixtures, MockScheduleJobService, OneStartAppPerSuite}
import models.news.Audience
import models.news.Audience.{DepartmentAudience, Staff}
import org.quartz.JobKey
import org.scalatestplus.play.PlaySpec


class NewsServiceTest extends PlaySpec with OneStartAppPerSuite {

  val newsService = get[NewsService]
  val categoryIds = get[NewsCategoryService].all().map(_.id)
  val scheduler = get[ScheduleJobService].asInstanceOf[MockScheduleJobService]

  val staffAudience = Audience(Seq(DepartmentAudience("IN", Seq(Staff))))
  val item = Fixtures.news.save()

  "NewsService" should {

    "save items" in {
      scheduler.reset()

      val id = newsService.save(item, Audience.Public, categoryIds.take(2))

      val render = newsService.getNewsItem(id).get

      render must have('title (item.title))

      scheduler.scheduledJobs.map(_.job.getKey) must contain(new JobKey(id, "PublishNewsItem"))
    }

    "update items" in {
      scheduler.reset()

      val id = newsService.save(item, Audience.Public, categoryIds.take(1))

      newsService.update(id, item, staffAudience, categoryIds.take(2))

      val render = newsService.getNewsItem(id).get

      render.categories.map(_.id) mustBe categoryIds.take(2)
      newsService.getAudience(id) must contain(staffAudience)

      val jobKey = new JobKey(id, "PublishNewsItem")
      scheduler.deletedJobs must contain(jobKey)
      scheduler.scheduledJobs.map(_.job.getKey) must contain(jobKey)
    }

  }

}
