package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.{PublishedNotification, PublishedNotificationSave}
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[PublishedNotificationsDaoImpl])
trait PublishedNotificationsDao {
  def getByActivityId(activityId: String)(implicit c: Connection): Option[PublishedNotification]

  def delete(activityId: String)(implicit c: Connection): Unit

  def save(publishedNotification: PublishedNotificationSave)(implicit c: Connection): Unit

  def update(publishedNotification: PublishedNotificationSave)(implicit c: Connection): Unit

  def getByPublisherId(publisherId: String)(implicit c: Connection): Seq[PublishedNotification]

}

@Singleton
class PublishedNotificationsDaoImpl extends PublishedNotificationsDao {

  override def getByActivityId(activityId: String)(implicit c: Connection) = {
    SQL"SELECT * FROM PUBLISHED_NOTIFICATION WHERE ACTIVITY_ID = $activityId"
      .executeQuery()
      .as(publishedNotificationParser.singleOpt)
  }

  override def delete(activityId: String)(implicit c: Connection) = {
    SQL"DELETE FROM PUBLISHED_NOTIFICATION WHERE ACTIVITY_ID = $activityId"
      .execute()
  }

  override def save(publishedNotification: PublishedNotificationSave)(implicit c: Connection) = {
    import publishedNotification._
    SQL"INSERT INTO PUBLISHED_NOTIFICATION (ACTIVITY_ID, PUBLISHER_ID, CREATED_AT, CREATED_BY) VALUES ($activityId, $publisherId, SYSDATE, ${changedBy.string})"
      .execute()
  }

  override def update(publishedNotification: PublishedNotificationSave)(implicit c: Connection) = {
    import publishedNotification._
    SQL"UPDATE PUBLISHED_NOTIFICATION SET PUBLISHER_ID = $publisherId, UPDATED_AT = SYSDATE, UPDATED_BY = ${changedBy.string} WHERE ACTIVITY_ID = $activityId"
      .execute()
  }

  override def getByPublisherId(publisherId: String)(implicit c: Connection) =
    SQL"SELECT * FROM PUBLISHED_NOTIFICATION WHERE PUBLISHER_ID = $publisherId"
      .executeQuery()
      .as(publishedNotificationParser.*)

  val publishedNotificationParser = for {
    activityId <- str("activity_id")
    publisherId <- str("publisher_id")
    createdAt <- get[DateTime]("created_at")
    updatedAt <- get[Option[DateTime]]("updated_at")
    createdBy <- str("created_by")
    updatedBy <- get[Option[String]]("updated_by")
  } yield PublishedNotification(
    activityId,
    publisherId,
    createdAt,
    Usercode(createdBy),
    updatedAt,
    updatedBy.map(Usercode)
  )

}

