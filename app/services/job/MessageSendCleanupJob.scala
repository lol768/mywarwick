package services.job

import com.google.inject.Inject
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
      messagingDao.deleteOldSuccessfulMessageSends()
    }

}
