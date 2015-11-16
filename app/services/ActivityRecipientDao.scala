package services

import java.sql.Connection

import anorm.SQL
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityRecipientDaoImpl])
trait ActivityRecipientDao {

  def create(activityId: String, usercode: String)(implicit c: Connection): Unit

  def markSent(activityId: String, usercode: String)(implicit c: Connection): Unit

}

class ActivityRecipientDaoImpl @Inject()(
  @NamedDatabase("default") val db: Database
) extends ActivityRecipientDao {

  override def create(activityId: String, usercode: String)(implicit c: Connection): Unit =
    SQL("INSERT INTO ACTIVITY_RECIPIENT (ACTIVITY_ID, USERCODE, CREATED_AT) VALUES ({activityId}, {usercode}, {createdAt})")
      .on(
        'activityId -> activityId,
        'usercode -> usercode,
        'createdAt -> DateTime.now()
      )
      .execute()

  override def markSent(activityId: String, usercode: String)(implicit c: Connection): Unit =
    SQL("UPDATE ACTIVITY_RECIPIENT SET SENT_AT = {sentAt} WHERE ACTIVITY_ID = {activityId} AND USERCODE = {usercode}")
      .on(
        'activityId -> activityId,
        'usercode -> usercode,
        'sentAt -> DateTime.now()
      )
      .execute()


}
