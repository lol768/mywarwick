package services.dao

import java.sql.Connection

import anorm.SQL
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityRecipientDaoImpl])
trait ActivityRecipientDao {

  def create(activityId: String, usercode: String, generatedAt: Option[DateTime])(implicit c: Connection): Unit

  def markSent(activityId: String, usercode: String)(implicit c: Connection): Unit

}

class ActivityRecipientDaoImpl @Inject()() extends ActivityRecipientDao {

  override def create(activityId: String, usercode: String, generatedAt: Option[DateTime])(implicit c: Connection): Unit = {
    val now = DateTime.now
    SQL("INSERT INTO ACTIVITY_RECIPIENT (ACTIVITY_ID, USERCODE, CREATED_AT, GENERATED_AT) VALUES ({activityId}, {usercode}, {createdAt}, {generatedAt})")
      .on(
        'activityId -> activityId,
        'usercode -> usercode,
        'createdAt -> now,
        'generatedAt -> generatedAt.getOrElse(now)
      )
      .execute()
  }

  override def markSent(activityId: String, usercode: String)(implicit c: Connection): Unit =
    SQL("UPDATE ACTIVITY_RECIPIENT SET SENT_AT = {sentAt} WHERE ACTIVITY_ID = {activityId} AND USERCODE = {usercode}")
      .on(
        'activityId -> activityId,
        'usercode -> usercode,
        'sentAt -> DateTime.now()
      )
      .execute()


}
