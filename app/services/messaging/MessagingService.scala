package services.messaging

import java.sql.Connection
import javax.inject.{Inject, Named, Provider}

import actors.MessageProcessing.ProcessingResult
import com.google.inject.ImplementedBy
import models._
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.MessagingDao
import services.elasticsearch.{ActivityESService, MessageSent}
import services.{ActivityService, EmailNotificationsPrefService, SmsNotificationsPrefService}
import system.Logging
import warwick.sso.{UserLookupService, Usercode}

import scala.concurrent.Future

/**
  * Handles message sending via a job table.
  *
  * The API will use send() to schedule a message send for some recipients.
  * Workers will lockRecord() and send the job to processNow() to actually get sent out.
  *
  * success(), failure(), or skipped() is then called to mark the job as done.
  */
@ImplementedBy(classOf[MessagingServiceImpl])
trait MessagingService {
  def send(recipients: Set[Usercode], activity: Activity): Unit

  def lockRecord(): Option[MessageSend.Light]

  def success(message: MessageSend.Light): Unit

  def failure(message: MessageSend.Light): Unit

  def skipped(message: MessageSend.Light): Unit

  def processNow(message: MessageSend.Light): Future[ProcessingResult]

  def getQueueStatus: Seq[QueueStatus]

  def getOldestUnsentMessageCreatedAt: Option[DateTime]

  def getSmsSentLast24Hours: Int
}

class MessagingServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  activitiesProvider: Provider[ActivityService],
  users: UserLookupService,
  emailNotificationsPrefService: EmailNotificationsPrefService,
  smsNotificationsPrefService: SmsNotificationsPrefService,
  @Named("email") emailer: OutputService,
  @Named("mobile") mobile: OutputService,
  @Named("sms") sms: OutputService,
  messagingDao: MessagingDao,
  activityESService: ActivityESService,
) extends MessagingService with Logging {

  // weak sauce way to resolve cyclic dependency.
  private lazy val activities = activitiesProvider.get

  override def send(recipients: Set[Usercode], activity: Activity): Unit = {
    def save(output: Output, user: Usercode)(implicit c: Connection) = {
      if (logger.isDebugEnabled) logger.logger.debug(s"Sending ${output.name} to $user about ${activity.id}")
      messagingDao.save(activity, user, output)
    }

    db.withTransaction { implicit c =>
      unmutedRecipients(recipients, activity).foreach { user =>
        if (sendEmailFor(user, activity)) {
          save(Output.Email, user)
        }
        if (sendSmsFor(user, activity)) {
          save(Output.SMS, user)
        }
        if (sendMobileFor(user, activity)) {
          save(Output.Mobile, user)
        }
      }
    }
  }

  def unmutedRecipients(recipients: Set[Usercode], activity: Activity): Set[Usercode] = {
    activities.getActivityRenderById(activity.id).map { activityRender =>
      val mutedUsercodes = recipients.intersect(
        activities.getActivityMutes(activityRender.activity, activityRender.tags, recipients)
          .map(_.usercode).toSet
      )
      if (mutedUsercodes.nonEmpty) {
        logger.info(s"Muted sending activity ${activity.id} to: ${mutedUsercodes.map(_.string).mkString(",")}")

        mutedUsercodes.foreach(activities.markSent(activity.id, _))
      }
      recipients.diff(mutedUsercodes)
    }.getOrElse(recipients)
  }

  /**
    * Attempts to mark a single MessageSend as taken, and returns it if it
    * was successful. Usually it will only return None when there are no messages
    * to send, but it is also possible when a number of other workers are locking
    * messages at once.
    */
  def lockRecord(): Option[MessageSend.Light] = {
    db.withTransaction { implicit connection =>
      messagingDao.lockRecord()
    }
  }

  def success(message: MessageSend.Light): Unit = complete(MessageState.Success, message)

  def failure(message: MessageSend.Light): Unit = complete(MessageState.Failure, message)

  def skipped(message: MessageSend.Light): Unit = complete(MessageState.Skipped, message)

  private def complete(newState: MessageState, message: MessageSend.Light): Unit = {
    import message._
    activityESService.indexMessageSentReq(MessageSent(activity, user, newState, output))
    db.withTransaction { implicit c =>
      messagingDao.complete(message.id, newState)
    }
  }

  def sendEmailFor(user: Usercode, activity: Activity): Boolean =
    emailNotificationsPrefService.get(user) &&
      activity.sendEmail.getOrElse(
        activities.getProvider(activity.providerId).exists(_.sendEmail)
      )

  def sendSmsFor(user: Usercode, activity: Activity): Boolean =
    smsNotificationsPrefService.get(user) && smsNotificationsPrefService.getNumber(user).nonEmpty

  def sendMobileFor(user: Usercode, activity: Activity): Boolean = true

  override def processNow(message: MessageSend.Light): Future[ProcessingResult] = {
    activities.markSent(message.activity, message.user)
    activities.getActivityById(message.activity).map { activity =>
      users.getUsers(Seq(message.user)).get.get(message.user).map { user =>
        val heavyMessage = message.fill(user, activity)
        heavyMessage.output match {
          case Output.Email => emailer.send(heavyMessage)
          case Output.SMS => sms.send(heavyMessage)
          case Output.Mobile => mobile.send(heavyMessage)
        }
      }.getOrElse {
        Future.successful(ProcessingResult(success = false, s"User ${message.user} not found"))
      }
    }.getOrElse {
      Future.successful(ProcessingResult(success = false, s"Activity ${message.activity} not found"))
    }
  }

  override def getQueueStatus: Seq[QueueStatus] =
    db.withConnection(implicit c => messagingDao.getQueueStatus())

  override def getOldestUnsentMessageCreatedAt: Option[DateTime] =
    db.withConnection(implicit c => messagingDao.getOldestUnsentMessageCreatedAt())

  override def getSmsSentLast24Hours: Int =
    db.withConnection(implicit c => messagingDao.getSmsSentLast24Hours())

}










