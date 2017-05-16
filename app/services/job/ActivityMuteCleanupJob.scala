package services.job

import com.google.inject.Inject
import org.joda.time.DateTime
import org.quartz.{Job, JobExecutionContext}
import play.api.db.{Database, NamedDatabase}
import services.dao.ActivityMuteDao
import system.Logging

class ActivityMuteCleanupJob @Inject()(
  activityMuteDao: ActivityMuteDao,
  @NamedDatabase("default") db: Database
) extends Job with Logging {

  override def execute(jobExecutionContext: JobExecutionContext): Unit =
    db.withConnection { implicit c =>
      val oneMonthAgo = DateTime.now.minusMonths(1)
      val itemsDeleted = activityMuteDao.deleteExpiredBefore(oneMonthAgo)

      logger.info(s"Deleted old ACTIVITY_MUTE row(s): deleted=$itemsDeleted, before=$oneMonthAgo")
    }

}
