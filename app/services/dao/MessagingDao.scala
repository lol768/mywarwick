package services.dao

import java.sql.Connection

import anorm.SQL
import com.google.inject.ImplementedBy
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[MessagingDaoImpl])
trait MessagingDao {

  def deleteMessagesSuccessfullySentBefore(date: DateTime)(implicit c: Connection): Int

}

class MessagingDaoImpl extends MessagingDao {

  override def deleteMessagesSuccessfullySentBefore(date: DateTime)(implicit c: Connection): Int = {
    SQL("DELETE FROM MESSAGE_SEND WHERE STATE = {state} AND UPDATED_AT < {date}")
      .on(
        'state -> "S",
        'date -> date
      )
      .executeUpdate()
  }

}
