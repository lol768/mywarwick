package services

import helpers.{Fixtures, MockSchedulerService, OneStartAppPerSuite}
import models.{Audience, AudienceSize}
import models.Audience._
import org.joda.time.DateTime
import org.quartz.JobKey
import org.quartz.SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
import org.scalatest.concurrent.Eventually.eventually
import helpers.BaseSpec
import services.job.PublishNewsItemJob


class NewsServiceTest extends BaseSpec with OneStartAppPerSuite {

  private val newsService = get[NewsService]
  private val categoryIds = get[NewsCategoryService].all().map(_.id)
  private val scheduler = get[SchedulerService].asInstanceOf[MockSchedulerService]

  private val staffAudience = Audience(Seq(DepartmentAudience("IN", Seq(Staff))))
  private val item = Fixtures.news.save().copy(publishDate = DateTime.now.plusDays(1))

  "NewsService" should {

    "save items" in {
      scheduler.reset()

      val id = newsService.save(item, Audience.Public, categoryIds.take(2))

      val render = newsService.getNewsItem(id).get
      render must have('title (item.title))

      val renderWithAudit = newsService.getNewsByPublisherWithAuditsAndAudience(item.publisherId, 100).find(_.id == id).get
      renderWithAudit.audienceSize mustBe AudienceSize.Public

      scheduler.scheduledJobs.map(_.job.getKey) must contain(new JobKey(id, PublishNewsItemJob.name))
      scheduler.scheduledJobs.map(_.trigger.getStartTime) must contain(item.publishDate.toDate)
      scheduler.scheduledJobs.map(_.trigger.getMisfireInstruction) must contain(MISFIRE_INSTRUCTION_FIRE_NOW)
    }

    "update items" in {
      scheduler.reset()

      val id = newsService.save(item, Audience.Public, categoryIds.take(1))

      newsService.update(id, item, staffAudience, categoryIds.take(2))

      val render = newsService.getNewsItem(id).get

      render.categories.map(_.id) mustBe categoryIds.take(2)
      newsService.getAudience(id) must contain(staffAudience)

      val audienceService = get[AudienceService]
      val renderWithAudit = newsService.getNewsByPublisherWithAuditsAndAudience(item.publisherId, 100).find(_.id == id).get
      renderWithAudit.audienceSize mustBe AudienceSize.Finite(audienceService.resolve(staffAudience).get.size)

      val jobKey = new JobKey(id, PublishNewsItemJob.name)
      scheduler.deletedJobs must contain(jobKey)
      scheduler.scheduledJobs.map(_.job.getKey) must contain(jobKey)
    }

    "trigger update job now for item with past publish date" in {
      scheduler.reset()

      val id = newsService.save(item, Audience.Public, categoryIds.take(1))

      newsService.update(id, item.copy(publishDate = DateTime.now.minusHours(2)), staffAudience, categoryIds.take(2))

      val jobKey = new JobKey(id, PublishNewsItemJob.name)

      eventually {
        scheduler.deletedJobs must contain(jobKey)
        scheduler.triggeredJobs.map(_.getKey) must contain(jobKey)
      }
    }

  }

}
