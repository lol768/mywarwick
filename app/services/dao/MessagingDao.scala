package services.dao

import java.sql.Connection

import anorm.SQL
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._

trait MessagingDao {

  def deleteOldSuccessfulMessageSends()(implicit c: Connection): Unit

}

class MessagingDaoImpl extends MessagingDao {

  override def deleteOldSuccessfulMessageSends()(implicit c: Connection): Unit =
    SQL("DELETE FROM MESSAGE_SEND WHERE STATE = {state} AND UPDATED_AT < {date}")
      .on(
        'state -> "S",
        'date -> DateTime.now.minusWeeks(1)
      )
      .execute()

}
