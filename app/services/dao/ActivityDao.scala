package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.Output.Mobile
import models._
import org.joda.time.DateTime
import system.DatabaseDialect
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityDaoImpl])
trait ActivityDao {
  def getActivitiesByPublisherId(publisherId: String, limit: Int)(implicit c: Connection): Seq[Activity]

  def getActivitiesByProviderId(providerId: String, limit: Int)(implicit c: Connection): Seq[Activity]

  def getPushNotificationsSinceDate(usercode: String, sinceDate: DateTime)(implicit c: Connection): Seq[Activity]

  def getActivitiesForUser(usercode: String, limit: Int, before: Option[DateTime] = None)(implicit c: Connection): Seq[ActivityResponse]

  def save(activity: ActivitySave, audienceId: String, replaces: Seq[String])(implicit c: Connection): String

  def update(id: String, activity: ActivitySave, audienceId: String)(implicit c: Connection): Unit

  def delete(activityId: String)(implicit c: Connection): Unit

  def getActivityById(id: String)(implicit c: Connection): Option[Activity] =
    getActivitiesByIds(Seq(id)).headOption

  def getActivitiesByIds(ids: Seq[String])(implicit c: Connection): Seq[Activity]

  def getLastReadDate(usercode: String)(implicit c: Connection): Option[DateTime]

  def countNotificationsSinceDate(usercode: String, date: DateTime)(implicit c: Connection): Int

  def saveLastReadDate(usercode: String, read: DateTime)(implicit c: Connection): Boolean

  def getActivityIcon(providerId: String)(implicit c: Connection): Option[ActivityIcon]
}

class ActivityDaoImpl @Inject()(
  dialect: DatabaseDialect
) extends ActivityDao {

  override def save(activity: ActivitySave, audienceId: String, replaces: Seq[String])(implicit c: Connection): String = {
    import activity._
    val id = UUID.randomUUID().toString
    val now = new DateTime()
    val publishedAtOrNow = publishedAt.getOrElse(now)

    SQL"""
      INSERT INTO ACTIVITY (id, provider_id, type, title, text, url, published_at, created_at, should_notify, audience_id, publisher_id, created_by)
      VALUES ($id, $providerId, ${`type`}, $title, $text, $url, $publishedAtOrNow, $now, $shouldNotify, $audienceId, $publisherId, ${changedBy.string})
    """
      .execute()

    updateReplacedActivity(id, replaces)

    id
  }

  override def update(id: String, activity: ActivitySave, audienceId: String)(implicit c: Connection): Unit = {
    import activity._
    val publishedAtOrNow = publishedAt.getOrElse(DateTime.now)
    SQL"UPDATE ACTIVITY SET TYPE = ${`type`}, TITLE = $title, TEXT = $text, URL = $url, PUBLISHED_AT = $publishedAtOrNow, AUDIENCE_ID = $audienceId WHERE ID = $id"
      .execute()
  }

  override def delete(activityId: String)(implicit c: Connection) = {
    SQL"DELETE FROM MESSAGE_SEND WHERE ACTIVITY_ID = $activityId".execute()
    SQL"DELETE FROM ACTIVITY_TAG WHERE ACTIVITY_ID = $activityId".execute()
    SQL"DELETE FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = $activityId".execute()
    SQL"DELETE FROM ACTIVITY WHERE ID = $activityId".execute()
  }

  def updateReplacedActivity(replacedById: String, replaces: Seq[String])(implicit c: Connection) =
    replaces.grouped(1000).foreach { group =>
      SQL("UPDATE ACTIVITY SET replaced_by_id = {replacedById} WHERE id IN ({replaces})")
        .on(
          'replacedById -> replacedById,
          'replaces -> group
        )
        .execute()
    }

  def getActivitiesByIds(ids: Seq[String])(implicit c: Connection): Seq[Activity] =
    ids.grouped(1000).flatMap { ids =>
      SQL("SELECT * FROM ACTIVITY WHERE id IN ({ids})")
        .on('ids -> ids)
        .as(activityParser.*)
    }.toSeq

  override def getActivitiesByProviderId(providerId: String, limit: Int)(implicit c: Connection): Seq[Activity] = {
    SQL(s"SELECT * FROM ACTIVITY WHERE PROVIDER_ID = {providerId} ORDER BY CREATED_AT DESC ${dialect.limitOffset(limit)}")
      .on('providerId -> providerId)
      .as(activityParser.*)
  }

  override def getActivitiesByPublisherId(publisherId: String, limit: Int)(implicit c: Connection): Seq[Activity] = {
    SQL(s"""
      SELECT * FROM ACTIVITY
      WHERE PUBLISHER_ID = {publisherId}
      ORDER BY CREATED_AT DESC
      ${dialect.limitOffset(limit)}
       """)
      .on('publisherId -> publisherId)
      .as(activityParser.*)
  }

  override def getPushNotificationsSinceDate(usercode: String, sinceDate: DateTime)(implicit c: Connection): Seq[Activity] = {
    SQL(
      """
      SELECT ACTIVITY.* FROM ACTIVITY JOIN ACTIVITY_RECIPIENT ON ACTIVITY_RECIPIENT.ACTIVITY_ID = ACTIVITY.ID
      WHERE USERCODE = {usercode} AND SHOULD_NOTIFY = 1 AND ACTIVITY_RECIPIENT.PUBLISHED_AT > {sinceDate} AND ACTIVITY.ID IN (
      SELECT ACTIVITY.ID FROM MESSAGE_SEND WHERE USERCODE = {usercode} AND OUTPUT = {mobile})
      """)
      .on(
        'usercode -> usercode,
        'sinceDate -> sinceDate,
        'mobile -> Mobile.name
      )
      .as(activityParser.*)
  }

  override def getActivitiesForUser(usercode: String, limit: Int, before: Option[DateTime] = None)(implicit c: Connection): Seq[ActivityResponse] = {
    val maybeBefore = if (before.isDefined) "AND ACTIVITY_RECIPIENT.PUBLISHED_AT < {before}" else ""
    val activities = SQL(
      s"""
        SELECT
          ACTIVITY.*,
          PROVIDER.ICON,
          PROVIDER.COLOUR,
          ACTIVITY_TAG.NAME          AS TAG_NAME,
          ACTIVITY_TAG.VALUE         AS TAG_VALUE,
          ACTIVITY_TAG.DISPLAY_VALUE AS TAG_DISPLAY_VALUE
        FROM ACTIVITY
          LEFT JOIN ACTIVITY_TAG ON ACTIVITY_TAG.ACTIVITY_ID = ACTIVITY.ID
          LEFT JOIN PROVIDER ON PROVIDER.ID = ACTIVITY.PROVIDER_ID
        WHERE ACTIVITY.ID IN (
          SELECT ACTIVITY_ID
          FROM ACTIVITY_RECIPIENT
            JOIN ACTIVITY ON ACTIVITY_RECIPIENT.ACTIVITY_ID = ACTIVITY.ID
          WHERE USERCODE = {usercode}
                AND REPLACED_BY_ID IS NULL
                $maybeBefore
          ORDER BY ACTIVITY_RECIPIENT.PUBLISHED_AT DESC
          ${dialect.limitOffset(limit)})
        """)
      .on(
        'usercode -> usercode,
        'before -> before.getOrElse(DateTime.now)
      )
      .as(activityResponseParser.*)

    combineActivities(activities)
  }

  def combineActivities(activities: Seq[ActivityResponse]): Seq[ActivityResponse] = {
    activities
      .groupBy(_.activity.id)
      .map { case (id, a) => a.reduceLeft((a1, a2) => a1.copy(tags = a1.tags ++ a2.tags)) }
      .toSeq
  }

  sealed abstract class ReadField(val name: String)

  case object Notifications extends ReadField("NOTIFICATIONS_LAST_READ")

  case object Activities extends ReadField("ACTIVITIES_LAST_READ")

  override def getLastReadDate(usercode: String)(implicit c: Connection): Option[DateTime] = {
    SQL(
      """
        SELECT NOTIFICATIONS_LAST_READ
        FROM ACTIVITY_RECIPIENT_READ
        WHERE USERCODE = {usercode}
      """)
      .on('usercode -> usercode)
      .as(get[DateTime]("NOTIFICATIONS_LAST_READ").singleOpt)
  }

  override def saveLastReadDate(usercode: String, newDate: DateTime)(implicit c: Connection): Boolean = {
    SQL("UPDATE ACTIVITY_RECIPIENT_READ SET NOTIFICATIONS_LAST_READ = GREATEST(NOTIFICATIONS_LAST_READ, {read}) WHERE USERCODE = {usercode}")
      .on('usercode -> usercode, 'read -> newDate)
      .executeUpdate() > 0 ||
      SQL("INSERT INTO ACTIVITY_RECIPIENT_READ (USERCODE, NOTIFICATIONS_LAST_READ) VALUES ({usercode}, {read})")
        .on('usercode -> usercode, 'read -> newDate)
        .executeUpdate() > 0
  }

  override def countNotificationsSinceDate(usercode: String, date: DateTime)(implicit c: Connection): Int =
    SQL("SELECT COUNT(ACTIVITY.ID) FROM ACTIVITY JOIN ACTIVITY_RECIPIENT ON ACTIVITY_RECIPIENT.ACTIVITY_ID = ACTIVITY.ID WHERE USERCODE = {usercode} AND SHOULD_NOTIFY = 1 AND ACTIVITY_RECIPIENT.PUBLISHED_AT > {date}")
      .on(
        'usercode -> usercode,
        'date -> date
      )
      .as(scalar[Int].single)

  private lazy val activityParser: RowParser[Activity] =
    get[String]("ID") ~
      get[String]("PROVIDER_ID") ~
      get[String]("TYPE") ~
      get[String]("TITLE") ~
      get[Option[String]]("TEXT") ~
      get[Option[String]]("URL") ~
      get[Option[String]]("REPLACED_BY_ID") ~
      get[DateTime]("PUBLISHED_AT") ~
      get[DateTime]("CREATED_AT") ~
      get[Boolean]("SHOULD_NOTIFY") ~
      get[Option[String]]("AUDIENCE_ID") map {
      case id ~ providerId ~ activityType ~ title ~ text ~ url ~ replacedById ~ publishedAt ~ createdAt ~ shouldNotify ~ audienceId =>
        Activity(id, providerId, activityType, title, text, url, replacedById, publishedAt, createdAt, shouldNotify, audienceId)
    }

  private lazy val tagParser: RowParser[Option[ActivityTag]] =
    get[Option[String]]("TAG_NAME") ~ // Option becbugs.elab.warwick.ac.ukause an activity can have no tags
      get[Option[String]]("TAG_VALUE") ~
      get[Option[String]]("TAG_DISPLAY_VALUE") map {
      case name ~ value ~ display =>
        for (name <- name; value <- value) yield ActivityTag(name, TagValue(value, display))
    }

  lazy val activityResponseParser: RowParser[ActivityResponse] =
    activityParser ~ activityIconParser.? ~ tagParser map {
      case activity ~ icon ~ tag => ActivityResponse(activity, icon, tag.toSeq)
    }

  override def getActivityIcon(providerId: String)(implicit c: Connection): Option[ActivityIcon] =
    SQL"SELECT icon, colour FROM PROVIDER WHERE ID = $providerId"
      .as(activityIconParser.singleOpt)

  private lazy val activityIconParser: RowParser[ActivityIcon] =
    get[String]("ICON") ~
    get[Option[String]]("COLOUR") map {
      case icon ~ colour => ActivityIcon(icon, colour)
    }
}
