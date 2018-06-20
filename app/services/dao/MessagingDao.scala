package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models._
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[MessagingDaoImpl])
trait MessagingDao {

  def save(activity: Activity, usercode: Usercode, output: Output, sendAt: Option[DateTime])(implicit c: Connection): Unit

  def complete(messageId: String, state: MessageState)(implicit c: Connection): Unit

  def deleteMessagesSuccessfullySentBefore(date: DateTime)(implicit c: Connection): Int

  def lockRecord()(implicit c: Connection): Option[MessageSend.Light]

  def getQueueStatus()(implicit c: Connection): Seq[QueueStatus]

  def getOldestUnsentMessageCreatedAt()(implicit c: Connection): Option[DateTime]

  def getSmsSentLast24Hours()(implicit c: Connection): Int

}

class MessagingDaoImpl extends MessagingDao {

  val queueParser: RowParser[QueueStatus] =
    get[String]("OUTPUT") ~
      get[String]("STATE") ~
      get[Int]("COUNT") map {
      case output ~ state ~ count =>
        QueueStatus(MessageState.parse(state), Output.parse(output), count)
    }

  override def getQueueStatus()(implicit c: Connection): Seq[QueueStatus] =
    SQL("SELECT OUTPUT, STATE, COUNT(*) AS COUNT FROM MESSAGE_SEND WHERE (STATE = 'F' AND UPDATED_AT > SYSDATE - 1) OR STATE != 'F' GROUP BY OUTPUT, STATE")
      .as(queueParser.*)

  override def getOldestUnsentMessageCreatedAt()(implicit c: Connection): Option[DateTime] =
    SQL("SELECT UPDATED_AT FROM MESSAGE_SEND WHERE STATE = 'A' AND ROWNUM = 1 ORDER BY UPDATED_AT ASC")
      .as(scalar[DateTime].singleOpt)

  override def getSmsSentLast24Hours()(implicit c: Connection): Int =
    SQL"SELECT COUNT(*) FROM MESSAGE_SEND WHERE OUTPUT = ${Output.SMS.name} AND UPDATED_AT > SYSDATE - 1"
      .as(scalar[Int].single)

  override def save(activity: Activity, usercode: Usercode, output: Output, sendAt: Option[DateTime])(implicit c: Connection): Unit = {
    SQL("INSERT INTO MESSAGE_SEND (ID, ACTIVITY_ID, USERCODE, OUTPUT, UPDATED_AT, SEND_AT) VALUES ({id}, {activityId}, {usercode}, {output}, {updatedAt}, {sendAt)")
      .on(
        'id -> UUID.randomUUID().toString,
        'activityId -> activity.id,
        'usercode -> usercode.string,
        'output -> output.name,
        'updatedAt -> DateTime.now,
        'sendAt -> sendAt.orNull
      )
      .execute()
  }

  override def complete(messageId: String, state: MessageState)(implicit c: Connection): Unit = {
    SQL("UPDATE MESSAGE_SEND SET STATE = {state}, UPDATED_AT = {updatedAt} WHERE ID = {id}")
      .on(
        'id -> messageId,
        'state -> state.dbValue,
        'updatedAt -> DateTime.now
      )
      .execute()
  }

  override def deleteMessagesSuccessfullySentBefore(date: DateTime)(implicit c: Connection): Int = {
    SQL("DELETE FROM MESSAGE_SEND WHERE STATE = {state} AND UPDATED_AT < {date}")
      .on(
        'state -> MessageState.Success.dbValue,
        'date -> date
      )
      .executeUpdate()
  }

  /**
    * Attempts to mark a single MessageSend as taken, and returns it if it
    * was successful. Usually it will only return None when there are no messages
    * to send, but it is also possible when a number of other workers are locking
    * messages at once.
    */
  def lockRecord()(implicit c: Connection): Option[MessageSend.Light] = {
    // 4 is arbitrary but any number > number of workers is good
    val select = SQL("SELECT * FROM MESSAGE_SEND WHERE STATE='A' AND (SEND_AT IS NULL OR SEND_AT <= SYSDATE) AND ROWNUM <= 4 FOR UPDATE")
    val update = SQL("UPDATE MESSAGE_SEND SET STATE='T', UPDATED_AT={now} WHERE ID={id} AND STATE='A'")

    val records = select.as(MessageSend.rowParser.*)
    // We've picked a few rows in case a few have already been taken.
    // Find the first one that we can successfully transition from A -> T
    records.find { record =>
      update.on('id -> record.id, 'now -> DateTime.now).executeUpdate() > 0
    }
  }

}
