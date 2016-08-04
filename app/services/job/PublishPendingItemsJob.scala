package services.job

import com.google.inject.Inject
import org.quartz.{Job, JobBuilder, JobExecutionContext}
import services.{ActivityService, AudienceService, NewsService, ScheduleJobService}

class PublishPendingItemsJob @Inject()(
  newsService: NewsService,
  activityService: ActivityService,
  audienceService: AudienceService,
  scheduleJobService: ScheduleJobService
) extends Job {

  override def execute(context: JobExecutionContext): Unit = {
    val newsItems = newsService.getNewsItemsToPublishNow()
    val activities = activityService.getActivitiesToPublishNow()

    newsItems.map { publish =>
      JobBuilder.newJob(classOf[PublishNewsItemJob])
        .usingJobData("newsItemId", publish.newsItemId)
        .usingJobData("audienceId", publish.audienceId)
        .build()
    }.foreach(scheduleJobService.triggerJobNow)

    activities.map { publish =>
      JobBuilder.newJob(classOf[PublishActivityJob])
        .usingJobData("activityId", publish.activityId)
        .usingJobData("audienceId", publish.audienceId)
        .build()
    }.foreach(scheduleJobService.triggerJobNow)
  }

}
