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

  def save(publishedNotification: PublishedNotificationSave)(implicit c: Connection): Unit

  def getByPublisherId(publisherId: String)(implicit c: Connection): Seq[PublishedNotification]

}

@Singleton
class PublishedNotificationsDaoImpl extends PublishedNotificationsDao {

  override def save(publishedNotification: PublishedNotificationSave)(implicit c: Connection) = {
    import publishedNotification._
    SQL"INSERT INTO PUBLISHED_NOTIFICATION (ACTIVITY_ID, PUBLISHER_ID, CREATED_AT, CREATED_BY, PUBLISHED_AT) VALUES ($activityId, $publisherId, SYSDATE, ${createdBy.string}, $publishedAt)"
      .execute()
  }

  override def getByPublisherId(publisherId: String)(implicit c: Connection) =
    SQL"SELECT * FROM PUBLISHED_NOTIFICATION WHERE PUBLISHER_ID = $publisherId"
      .executeQuery()
      .as(publishedNotificationParser.*)

  val publishedNotificationParser = str("activity_id") ~ str("publisher_id") ~ get[DateTime]("created_at") ~ str("created_by") ~ get[DateTime]("published_at") map {
    case (activityId ~ publisherId ~ createdAt ~ createdBy ~ publishedAt) =>
      PublishedNotification(activityId, publisherId, createdAt, Usercode(createdBy), publishedAt)
  }

}


