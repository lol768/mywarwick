package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models._
import org.joda.time.DateTime
import system.DatabaseDialect
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityDaoImpl])
trait ActivityDao {
  def getActivitiesForUser(usercode: String, limit: Int, before: Option[DateTime] = None)(implicit c: Connection): Seq[ActivityResponse]

  def save(activity: ActivityPrototype, replaces: Seq[String])(implicit c: Connection): String

  def getActivityById(id: String)(implicit c: Connection): Option[Activity] =
    getActivitiesByIds(Seq(id)).headOption

  def getActivitiesByIds(ids: Seq[String])(implicit c: Connection): Seq[Activity]

}

class ActivityDaoImpl @Inject()(
  dialect: DatabaseDialect
) extends ActivityDao {

  override def save(activity: ActivityPrototype, replaces: Seq[String])(implicit c: Connection): String = {
    import activity._
    val id = UUID.randomUUID().toString
    val now = new DateTime()

    SQL("INSERT INTO ACTIVITY (id, provider_id, type, title, text, generated_at, created_at, should_notify) VALUES ({id}, {providerId}, {type}, {title}, {text}, {generatedAt}, {createdAt}, {shouldNotify})")
      .on(
        'id -> id,
        'providerId -> providerId,
        'type -> `type`,
        'title -> title,
        'text -> text,
        'generatedAt -> generatedAt.getOrElse(now),
        'createdAt -> now,
        'shouldNotify -> shouldNotify
      )
      .execute()

    updateReplacedActivity(id, replaces)

    id
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

  override def getActivitiesForUser(usercode: String, limit: Int, before: Option[DateTime] = None)(implicit c: Connection): Seq[ActivityResponse] = {
    val maybeBefore = if (before.isDefined) "AND ACTIVITY_RECIPIENT.GENERATED_AT < {before}" else ""
    val activities = SQL(
      s"""
        SELECT
          ACTIVITY.*,
          ACTIVITY_TAG.NAME          AS TAG_NAME,
          ACTIVITY_TAG.VALUE         AS TAG_VALUE,
          ACTIVITY_TAG.DISPLAY_VALUE AS TAG_DISPLAY_VALUE
        FROM ACTIVITY
          LEFT JOIN ACTIVITY_TAG ON ACTIVITY_TAG.ACTIVITY_ID = ACTIVITY.ID
        WHERE ACTIVITY.ID IN (
          SELECT ACTIVITY_ID
          FROM ACTIVITY_RECIPIENT
            JOIN ACTIVITY ON ACTIVITY_RECIPIENT.ACTIVITY_ID = ACTIVITY.ID
          WHERE USERCODE = {usercode}
                AND REPLACED_BY_ID IS NULL
                $maybeBefore
          ORDER BY ACTIVITY_RECIPIENT.GENERATED_AT DESC
          ${dialect.limit("{limit}")})
        """)
      .on(
        'usercode -> usercode,
        'before -> before.getOrElse(DateTime.now),
        'limit -> limit
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

  private lazy val activityParser: RowParser[Activity] = {
    get[String]("ID") ~
      get[String]("PROVIDER_ID") ~
      get[String]("TYPE") ~
      get[String]("TITLE") ~
      get[String]("TEXT") ~
      get[Option[String]]("REPLACED_BY_ID") ~
      get[DateTime]("GENERATED_AT") ~
      get[DateTime]("CREATED_AT") ~
      get[Boolean]("SHOULD_NOTIFY") map {
      case id ~ providerId ~ activityType ~ title ~ text ~ replacedById ~ generatedAt ~ createdAt ~ shouldNotify =>
        Activity(id, providerId, activityType, title, text, replacedById, generatedAt, createdAt, shouldNotify)
    }
  }

  private lazy val tagParser: RowParser[Option[ActivityTag]] = {
    get[Option[String]]("TAG_NAME") ~ // Option because an activity can have no tags
    get[Option[String]]("TAG_VALUE") ~
    get[Option[String]]("TAG_DISPLAY_VALUE") map {
      case name ~ value ~ display =>
        (for (name <- name; value <- value) yield ActivityTag(name, TagValue(value, display)))
    }
  }

  lazy val activityResponseParser: RowParser[ActivityResponse] = {
    activityParser ~ tagParser map {
      case activity ~ tag => ActivityResponse(activity, tag.toSeq)
    }
  }
}
