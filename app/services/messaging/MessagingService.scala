package services.messaging

import java.sql.Connection
import javax.inject.{Inject, Named, Provider}

import actors.MessageProcessing.ProcessingResult
import com.google.inject.ImplementedBy
import models._
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.ActivityService
import services.dao.MessagingDao
import system.Logging
import warwick.sso.{UserLookupService, Usercode}

import scala.concurrent.Future

/**
  * Handles message sending via a job table.
  *
  * The API will use send() to schedule a message send for some recipients.
  * Workers will lockRecord() and send the job to processNow() to actually get sent out.
  *
  * success() or failure() is then called to mark the job as done.
  */
@ImplementedBy(classOf[MessagingServiceImpl])
trait MessagingService {
  def send(recipients: Set[Usercode], activity: Activity): Unit

  def lockRecord(): Option[MessageSend.Light]

  def success(message: MessageSend.Light): Unit

  def failure(message: MessageSend.Light): Unit

  def processNow(message: MessageSend.Light): Future[ProcessingResult]

  def getQueueStatus(): Seq[QueueStatus]

  def getOldestUnsentMessageCreatedAt(): Option[DateTime]
}

class MessagingServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  activitiesProvider: Provider[ActivityService],
  users: UserLookupService,
  @Named("email") emailer: OutputService,
  @Named("mobile") mobile: OutputService,
  messagingDao: MessagingDao
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

  private def complete(newState: MessageState, message: MessageSend.Light): Unit = {
    db.withTransaction { implicit c =>
      messagingDao.complete(message.id, newState)
    }
  }

  // TODO actually decide whether this user should receive this sort of notification
  def sendEmailFor(user: Usercode, activity: Activity): Boolean = true

  def sendSmsFor(user: Usercode, activity: Activity): Boolean = false

  def sendMobileFor(user: Usercode, activity: Activity): Boolean = true

  override def processNow(message: MessageSend.Light): Future[ProcessingResult] = {
    activities.getActivityById(message.activity).map { activity =>
      users.getUsers(Seq(message.user)).get.get(message.user).map { user =>
        val heavyMessage = message.fill(user, activity)
        heavyMessage.output match {
          case Output.Email => emailer.send(heavyMessage)
          case Output.SMS => Future.successful(ProcessingResult(success = false, "SMS not yet supported"))
          case Output.Mobile => mobile.send(heavyMessage)
        }
      }.getOrElse {
        Future.successful(ProcessingResult(success = false, s"User ${message.user} not found"))
      }
    }.getOrElse {
      Future.successful(ProcessingResult(success = false, s"Activity ${message.activity} not found"))
    }
  }

  override def getQueueStatus(): Seq[QueueStatus] =
    db.withConnection(implicit c => messagingDao.getQueueStatus())

  override def getOldestUnsentMessageCreatedAt() =
    db.withConnection(implicit c => messagingDao.getOldestUnsentMessageCreatedAt())

}










