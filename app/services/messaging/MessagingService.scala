package services.messaging

import java.sql.Connection
import java.util.UUID
import javax.inject.{Inject, Named}

import actors.MessageProcessing.ProcessingResult
import com.google.inject.ImplementedBy
import models.Activity
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import system.Logging
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

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
  def lockRecord(): Option[MessageSend]

  def success(message: MessageSend): Unit
  def failure(message: MessageSend): Unit

  def processNow(message: MessageSend): Future[ProcessingResult]
}

class MessagingServiceImpl @Inject() (
  @NamedDatabase("default") db: Database,
  @Named("email") emailer: OutputService
) extends MessagingService with Logging {

  import anorm._

  override def send(recipients: Set[Usercode], activity: Activity): Unit = {
    def save(output: Output, user: Usercode)(implicit c: Connection) = {
      if (logger.isDebugEnabled) logger.logger.debug(s"Sending ${output.name} to ${user} about ${activity.id}")
      SQL"""INSERT INTO MESSAGE_SEND (ID, NOTIFICATION_ID, USERCODE, OUTPUT, UPDATED_AT) VALUES
            ( ${UUID.randomUUID().toString}, ${activity.id}, ${user.string}, ${Output.Email.name}, ${new DateTime()} )
          """.execute()
    }

    db.withTransaction { implicit c =>
      recipients.foreach { user =>
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

  /**
    * Attempts to mark a single MessageSend as taken, and returns it if it
    * was successful. Usually it will only return None when there are no messages
    * to send, but it is also possible when a number of other workers are locking
    * messages at once.
    */
  def lockRecord(): Option[MessageSend] = {
    db.withTransaction { implicit connection =>
      // 4 is arbitrary but any number > number of workers is good
      val select = SQL("SELECT * FROM MESSAGE_SEND WHERE STATE='A' AND ROWNUM <= 4 FOR UPDATE")
      val update = SQL("UPDATE MESSAGE_SEND SET STATE='T', UPDATED_AT={now} WHERE ID={id} AND STATE='A'")
      val now = new DateTime

      val records = select.as(MessageSend.rowParser.*)
      // We've picked a few rows in case a few have already been taken.
      // Find the first one that we can successfully transition from A -> T
      records.find { record =>
        update.on('id -> record.id, 'now -> now).executeUpdate() > 0
      }
    }
  }

  def success(message: MessageSend): Unit = complete("S", message)
  def failure(message: MessageSend): Unit = complete("F", message)

  private def complete(newState: String, message: MessageSend): Unit = {
    db.withTransaction { implicit c =>
      SQL"UPDATE MESSAGE_SEND SET STATE=${newState}, UPDATED_AT=${new DateTime()} WHERE ID=${message.id}".executeUpdate()
    }
  }

  // TODO actually decide whether this user should receive this sort of notification
  def sendEmailFor(user: Usercode, activity: Activity): Boolean = true
  def sendSmsFor(user: Usercode, activity: Activity): Boolean = false
  def sendMobileFor(user: Usercode, activity: Activity): Boolean = false

  override def processNow(message: MessageSend): Future[ProcessingResult] = message.output match {
    case Output.Email => emailer.send(message)
    case Output.SMS => Future.successful(ProcessingResult(success=false, "SMS not yet supported"))
    case Output.Mobile => Future.successful(ProcessingResult(success=false, "Mobile not yet supported"))
  }
}










