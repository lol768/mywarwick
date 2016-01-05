package services.job

import com.google.inject.Inject
import org.joda.time.DateTime
import org.quartz.{Job, JobExecutionContext}
import play.api.db.{Database, NamedDatabase}
import services.dao.MessagingDao
import system.Logging

class MessageSendCleanupJob @Inject()(
  messagingDao: MessagingDao,
  @NamedDatabase("default") db: Database
) extends Job with Logging {

  override def execute(jobExecutionContext: JobExecutionContext): Unit =
    db.withConnection { implicit c =>
      val oneWeekAgo = DateTime.now.minusWeeks(1)
      val itemsDeleted = messagingDao.deleteMessagesSuccessfullySentBefore(oneWeekAgo)

      logger.info(s"Deleted old MESSAGE_SEND row(s): deleted=$itemsDeleted, before=$oneWeekAgo")
    }

}
