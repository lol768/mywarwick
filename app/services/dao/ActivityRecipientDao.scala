package services.dao

import java.sql.Connection

import anorm.SqlParser._
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

  def create(activityId: String, usercode: String, publishedAt: Option[DateTime], shouldNotify: Boolean)(implicit c: Connection): Unit

  def markProcessed(activityId: String, usercode: String)(implicit c: Connection): Unit

}

class ActivityRecipientDaoImpl @Inject()() extends ActivityRecipientDao {

  override def create(activityId: String, usercode: String, publishedAt: Option[DateTime], shouldNotify: Boolean)(implicit c: Connection): Unit = {
    val now = DateTime.now
    val published: DateTime = publishedAt.getOrElse(now)
    SQL"""INSERT INTO ACTIVITY_RECIPIENT (ACTIVITY_ID, USERCODE, CREATED_AT, PUBLISHED_AT, SHOULD_NOTIFY)
         VALUES ($activityId, $usercode, $now, $published, $shouldNotify)""".execute()
  }

  override def markProcessed(activityId: String, usercode: String)(implicit c: Connection): Unit = {
    val now = DateTime.now
    val updated = SQL"UPDATE ACTIVITY_RECIPIENT SET SENT_AT = $now WHERE SENT_AT IS NULL AND ACTIVITY_ID = $activityId AND USERCODE = $usercode"
      .executeUpdate()

    if (updated > 0) {
      SQL"UPDATE ACTIVITY SET SENT_COUNT = SENT_COUNT + 1 WHERE ID = $activityId"
        .execute()
    }
  }

  override def setRecipients(activity: Activity, recipients: Set[Usercode])(implicit c: Connection): Unit = {
    SQL"DELETE FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = ${activity.id}".execute()
    recipients.foreach { recipient =>
      create(activity.id, recipient.string, Some(activity.publishedAt), activity.shouldNotify)
    }
  }

}
