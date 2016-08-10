package services.dao

import java.sql.Connection

import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.Activity
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[ActivityRecipientDaoImpl])
trait ActivityRecipientDao {

  /**
    * Sets the recipients for the activity, deleting any existing recipients.
    */
  def setRecipients(activity: Activity, recipients: Set[Usercode])(implicit c: Connection): Unit

  def create(activityId: String, usercode: String, publishedAt: Option[DateTime])(implicit c: Connection): Unit

  def markSent(activityId: String, usercode: String)(implicit c: Connection): Unit

}

class ActivityRecipientDaoImpl @Inject()() extends ActivityRecipientDao {

  override def create(activityId: String, usercode: String, publishedAt: Option[DateTime])(implicit c: Connection): Unit = {
    val now = DateTime.now
    SQL("INSERT INTO ACTIVITY_RECIPIENT (ACTIVITY_ID, USERCODE, CREATED_AT, PUBLISHED_AT) VALUES ({activityId}, {usercode}, {createdAt}, {publishedAt})")
      .on(
        'activityId -> activityId,
        'usercode -> usercode,
        'createdAt -> now,
        'publishedAt -> publishedAt.getOrElse(now)
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

  override def setRecipients(activity: Activity, recipients: Set[Usercode])(implicit c: Connection): Unit = {
    SQL"DELETE FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = ${activity.id}".execute()
    recipients.foreach { recipient =>
      create(activity.id, recipient.string, Some(activity.publishedAt))
    }
  }



}
