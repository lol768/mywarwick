package services.dao

import java.lang.{Integer => JInt}
import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.Output.Mobile
import models._
import models.publishing.PublisherActivityCount
import org.joda.time.DateTime
import system.DatabaseDialect
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

@ImplementedBy(classOf[ActivityDaoImpl])
trait ActivityDao {
  def getPastActivitiesByPublisherId(publisherId: String, limit: Int, includeApiUser: Boolean)(implicit c: Connection): Seq[ActivityRender]

  def getSendingActivitiesByPublisherId(publisherId: String, limit: Int, includeApiUser: Boolean)(implicit c: Connection): Seq[ActivityRender]

  def getFutureActivitiesByPublisherId(publisherId: String, limit: Int, includeApiUser: Boolean)(implicit c: Connection): Seq[ActivityRender]

  def getPushNotificationsSinceDate(usercode: String, sinceDate: DateTime)(implicit c: Connection): Seq[Activity]

  def getActivitiesForUser(usercode: String, notifications: Boolean, before: Option[String] = None, since: Option[String] = None, limit: Int = 20)(implicit c: Connection): Seq[ActivityRender]

  def getActivityRenderById(id: String)(implicit c: Connection): Option[ActivityRender]

  def save(activity: ActivitySave, audienceId: String, audienceSize: AudienceSize, replaces: Seq[String])(implicit c: Connection): String

  def update(id: String, activity: ActivitySave, audienceId: String, audienceSize: AudienceSize)(implicit c: Connection): Unit

  def delete(activityId: String)(implicit c: Connection): Unit

  def getActivityById(id: String)(implicit c: Connection): Option[Activity] =
    getActivitiesByIds(Seq(id)).headOption

  def getActivitiesForDateTimeRange(from: DateTime, to: DateTime)(implicit c: Connection): Seq[Activity]

  def getActivitiesByIds(ids: Seq[String])(implicit c: Connection): Seq[Activity]

  def getLastReadDate(usercode: String)(implicit c: Connection): Option[DateTime]

  def countNotificationsSinceDate(usercode: String, date: DateTime)(implicit c: Connection): Int

  def saveLastReadDate(usercode: String, read: DateTime)(implicit c: Connection): Boolean

  def getActivityIcon(providerId: String)(implicit c: Connection): Option[ActivityIcon]

  def allProviders(implicit c: Connection): Seq[ActivityProvider]

  def getProvider(id: String)(implicit c: Connection): Option[ActivityProvider]

  def countNotificationsSinceDateGroupedByPublisher(activityType: String, since: DateTime)(implicit c: Connection): Seq[PublisherActivityCount]

  def updateAudienceCount(activityId: String, audienceSize: AudienceSize)(implicit c: Connection): Int

  def getAudienceSizes(ids: Seq[String])(implicit c: Connection): Map[String, AudienceSize]

  def getSentCounts(ids: Seq[String])(implicit c: Connection): Map[String, Int]

  def setSentCount(id: String, count: Int)(implicit c: Connection): Unit

  def getActivityReadCountSincePublishedDate(activityId: String)(implicit c: Connection): Int
}

class ActivityDaoImpl @Inject()(
  dialect: DatabaseDialect
) extends ActivityDao {

  override def save(activity: ActivitySave, audienceId: String, audienceSize: AudienceSize, replaces: Seq[String])(implicit c: Connection): String = {
    import activity._
    val id = UUID.randomUUID().toString
    val now = DateTime.now
    val publishedAtOrNow = publishedAt.getOrElse(now)
    val sendEmailObj = sendEmail.map[JInt] {
      if (_) 1 else 0
    }.orNull

    SQL"""
      INSERT INTO ACTIVITY (id, provider_id, type, title, text, url, published_at, created_at, should_notify, audience_id, publisher_id, created_by, send_email, audience_size, api)
      VALUES ($id, $providerId, ${`type`}, $title, $text, $url, $publishedAtOrNow, $now, $shouldNotify, $audienceId, $publisherId, ${changedBy.string}, $sendEmailObj, ${audienceSize.toOption}, $api)
    """
      .execute()

    updateReplacedActivity(id, replaces)

    id
  }

  override def update(id: String, activity: ActivitySave, audienceId: String, audienceSize: AudienceSize)(implicit c: Connection): Unit = {
    import activity._
    val publishedAtOrNow = publishedAt.getOrElse(DateTime.now)
    SQL"UPDATE ACTIVITY SET TYPE = ${`type`}, TITLE = $title, TEXT = $text, URL = $url, PUBLISHED_AT = $publishedAtOrNow, AUDIENCE_ID = $audienceId, AUDIENCE_SIZE = ${audienceSize.toOption}, API = $api WHERE ID = $id"
      .execute()
  }

  override def delete(activityId: String)(implicit c: Connection) = {
    SQL"DELETE FROM MESSAGE_SEND WHERE ACTIVITY_ID = $activityId".execute()
    SQL"DELETE FROM ACTIVITY_TAG WHERE ACTIVITY_ID = $activityId".execute()
    SQL"DELETE FROM ACTIVITY_RECIPIENT WHERE ACTIVITY_ID = $activityId".execute()
    SQL"DELETE FROM ACTIVITY WHERE ID = $activityId".execute()
  }

  override def getActivitiesForDateTimeRange(from: DateTime, to: DateTime)(implicit c: Connection): Seq[Activity] = {
    SQL(
      """
         SELECT * from ACTIVITY
         WHERE PUBLISHED_AT >= {from}
         AND PUBLISHED_AT <= {to}
      """
    ).on(
      'from -> from,
      'to -> to
    ).as(activityParser.*)
  }

  def updateReplacedActivity(replacedById: String, replaces: Seq[String])(implicit c: Connection) =
    replaces.grouped(1000).foreach { group =>
      SQL"UPDATE ACTIVITY SET replaced_by_id = $replacedById WHERE id IN ($replaces)"
        .execute()
      SQL"UPDATE ACTIVITY_RECIPIENT SET replaced = 1 WHERE ACTIVITY_ID IN ($replaces)"
        .execute()
    }

  def getActivitiesByIds(ids: Seq[String])(implicit c: Connection): Seq[Activity] =
    ids.grouped(1000).flatMap { ids =>
      SQL("SELECT * FROM ACTIVITY WHERE id IN ({ids})")
        .on('ids -> ids)
        .as(activityParser.*)
    }.toSeq

  override def getPastActivitiesByPublisherId(publisherId: String, limit: Int, includeApiUser: Boolean)(implicit c: Connection): Seq[ActivityRender] = combineActivities {
    SQL(
      s"""
      $selectActivityRender
      WHERE ACTIVITY.ID IN (
        ${
        dialect.limitOffset(limit)(
          s"""SELECT
            --+ INDEX(ACTIVITY, ACTIVITY_PUBLISHER_TIME_INDEX)
            ID FROM ACTIVITY
          WHERE ACTIVITY.PUBLISHER_ID = {publisherId}
            AND ACTIVITY.PUBLISHED_AT <= {now}
            AND ACTIVITY.SENT_COUNT = ACTIVITY.AUDIENCE_SIZE
            ${ if (!includeApiUser) "AND ACTIVITY.API = 0" else ""}
          ORDER BY ACTIVITY.PUBLISHED_AT DESC"""
        )
      }
      )
      ORDER BY ACTIVITY.PUBLISHED_AT DESC
      """)
      .on('publisherId -> publisherId, 'now -> DateTime.now)
      .as(activityRenderParser.*)
  }

  override def getSendingActivitiesByPublisherId(publisherId: String, limit: Int, includeApiUser: Boolean)(implicit c: Connection): Seq[ActivityRender] = combineActivities {
    SQL(
      s"""
      $selectActivityRender
      WHERE ACTIVITY.ID IN (
        ${
        dialect.limitOffset(limit)(
          s"""SELECT
            --+ INDEX(ACTIVITY, ACTIVITY_PUBLISHER_TIME_INDEX)
            ID FROM ACTIVITY
          WHERE ACTIVITY.PUBLISHER_ID = {publisherId}
            AND ACTIVITY.PUBLISHED_AT <= {now}
            AND ACTIVITY.SENT_COUNT < ACTIVITY.AUDIENCE_SIZE
            ${ if (!includeApiUser) "AND ACTIVITY.API = 0" else ""}
          ORDER BY ACTIVITY.PUBLISHED_AT DESC"""
        )
      }
      )
      ORDER BY ACTIVITY.PUBLISHED_AT DESC
      """)
      .on('publisherId -> publisherId, 'now -> DateTime.now)
      .as(activityRenderParser.*)
  }

  override def getFutureActivitiesByPublisherId(publisherId: String, limit: Int, includeApiUser: Boolean)(implicit c: Connection): Seq[ActivityRender] = combineActivities {
    SQL(
      s"""
      $selectActivityRender
      WHERE ACTIVITY.ID IN (
        ${
        dialect.limitOffset(limit)(
          s"""SELECT
            --+ INDEX(ACTIVITY, ACTIVITY_PUBLISHER_TIME_INDEX)
            ID FROM ACTIVITY
          WHERE ACTIVITY.PUBLISHER_ID = {publisherId}
            AND ACTIVITY.PUBLISHED_AT > {now}
            ${if (!includeApiUser) "AND ACTIVITY.API = 0" else ""}
          ORDER BY ACTIVITY.PUBLISHED_AT DESC"""
        )
      }
      )
      ORDER BY ACTIVITY.PUBLISHED_AT DESC
      """)
      .on('publisherId -> publisherId, 'now -> DateTime.now)
      .as(activityRenderParser.*)
  }

  override def getPushNotificationsSinceDate(usercode: String, sinceDate: DateTime)(implicit c: Connection): Seq[Activity] = {
    SQL"""
      SELECT ACTIVITY.* FROM ACTIVITY JOIN ACTIVITY_RECIPIENT ON ACTIVITY_RECIPIENT.ACTIVITY_ID = ACTIVITY.ID
      WHERE USERCODE = $usercode AND ACTIVITY_RECIPIENT.SHOULD_NOTIFY = 1 AND ACTIVITY_RECIPIENT.PUBLISHED_AT > $sinceDate AND ACTIVITY.ID IN (
      SELECT ACTIVITY.ID FROM MESSAGE_SEND WHERE USERCODE = $usercode AND OUTPUT = ${Mobile.name}
      )
      """
      .as(activityParser.*)
  }

  override def getActivitiesForUser(usercode: String, notifications: Boolean, before: Option[String], since: Option[String], limit: Int)(implicit c: Connection): Seq[ActivityRender] = combineActivities {
    // If before or since are specified, get the publish date for each
    val beforeDate = before.flatMap(getActivityById).map(_.publishedAt)
    val sinceDate = since.flatMap(getActivityById).map(_.publishedAt)

    // If the before activity exists, SQL to find only activities published before it
    val maybeBefore = if (beforeDate.nonEmpty)
      """
        AND (
          PUBLISHED_AT < {beforeDate} OR (
            PUBLISHED_AT = {beforeDate} AND ACTIVITY_ID < {before}
          )
        )
      """
    else ""

    // If the since activity exists, SQL to find only activities published after it
    val maybeSince = if (sinceDate.nonEmpty)
      """
        AND (
          PUBLISHED_AT > {sinceDate} OR (
            PUBLISHED_AT = {sinceDate} AND ACTIVITY_ID > {since}
          )
        )
      """
    else ""

    // Handle the case where there is an upper and lower bound on the activity
    // to return, and they have the same publish time
    val conditions = if (beforeDate.nonEmpty && beforeDate == sinceDate)
      """
        AND PUBLISHED_AT = {beforeDate}
        AND ACTIVITY_ID > {since}
        AND ACTIVITY_ID < {before}
      """
    else if (beforeDate.isEmpty && sinceDate.isEmpty)
      // If we don't specify either it can cause high load on the DB
      // This normally happens on a fresh app install so limit to the past 12 months
      // This will cause no activity to display if the oldest is longer than 12 months ago
      """
        AND PUBLISHED_AT > {oneYearAgo}
      """
    else maybeBefore + maybeSince

    // If requesting activities since an activity, return the activities
    // immediately after the since activity - not just any that have happened
    // since.
    // Note that the returned activities are always sorted newest-first.  This
    // affects which activities are returned.
    val publishedAtOrder = if (sinceDate.nonEmpty) "ASC" else "DESC"

    /**
      * It's important that the inner query here (which gets the final 100 or whatever IDs
      * we'll be returning) is fast, which is why:
      *  - It only uses columns that are in the ACTIVITY_RECIPIENT_READ_INDEX,
      * so it can read the index and not need to read the table at all;
      *  - It doesn't join on any other tables.
      *
      * If you're about to make a change that might break these two rules, it requires
      * some discussion before you go ahead.
      */
    val query =
      s"""
      $selectActivityRender
      WHERE ACTIVITY.ID IN (
        ${
        dialect.limitOffset(limit)(
          s"""SELECT ACTIVITY_ID
          FROM ACTIVITY_RECIPIENT
          WHERE USERCODE = {usercode}
            AND REPLACED_BY_ID IS NULL
            AND SHOULD_NOTIFY = {notifications}
            $conditions
          ORDER BY PUBLISHED_AT $publishedAtOrder, ACTIVITY_ID ASC"""
        )
      }
      )
      ORDER BY ACTIVITY.PUBLISHED_AT DESC, ACTIVITY.ID ASC
      """

    SQL(query).on(
      'usercode -> usercode,
      'before -> before.orNull,
      'since -> since.orNull,
      'beforeDate -> beforeDate.orNull,
      'sinceDate -> sinceDate.orNull,
      'notifications -> notifications,
      'oneYearAgo -> DateTime.now().minusYears(1)
    )
      .as(activityRenderParser.*)
  }

  override def getActivityRenderById(id: String)(implicit c: Connection): Option[ActivityRender] =
    combineActivities {
      SQL(s"$selectActivityRender WHERE ACTIVITY.ID = {id}")
        .on('id -> id)
        .as(activityRenderParser.*)
    }.headOption

  private def combineActivities(activities: Seq[ActivityRender]): Seq[ActivityRender] = {
    foldMerge[ActivityRender, String](activities, a => a.activity.id, (a1, a2) => a1.copy(tags = a1.tags ++ a2.tags))
  }

  // This is an amalgamation of groupBy and foldRight that preserves the original
  // ordering of the input sequence
  private def foldMerge[A, K](seq: Seq[A], key: A => K, merge: (A, A) => A): Seq[A] = {
    seq.foldRight(List.empty[A]) {
      case (a, x :: xs) if key(a) == key(x) => merge(a, x) :: xs
      case (a, xs) => a :: xs
    }
  }

  override def getActivityReadCountSincePublishedDate(activityId: String)(implicit c: Connection): Int =
    SQL(
      s"""
        SELECT COUNT(*) FROM ACTIVITY_RECIPIENT_READ r JOIN ACTIVITY_RECIPIENT a ON r.USERCODE = a.USERCODE
        WHERE ACTIVITY_ID = '$activityId'
        AND NOTIFICATIONS_LAST_READ > PUBLISHED_AT
      """)
      .as(scalar[Int].single)

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
    SQL"""
         SELECT COUNT(*) FROM ACTIVITY_RECIPIENT
         WHERE USERCODE = $usercode
         AND SHOULD_NOTIFY = 1
         AND PUBLISHED_AT > $date
      """
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
      get[String]("CREATED_BY") ~
      get[Boolean]("SHOULD_NOTIFY") ~
      get[Boolean]("API") ~
      get[Option[String]]("AUDIENCE_ID") ~
      get[Option[String]]("PUBLISHER_ID") ~
      get[Option[Boolean]]("SEND_EMAIL") map {
      case id ~ providerId ~ activityType ~ title ~ text ~ url ~ replacedById ~ publishedAt ~ createdAt ~ createdBy ~ shouldNotify ~ api ~ audienceId ~ publisherId ~ sendEmail =>
        Activity(id, providerId, activityType, title, text, url, replacedById, publishedAt, createdAt, Usercode(createdBy), shouldNotify, api, audienceId, publisherId, sendEmail)
    }

  private lazy val tagParser: RowParser[Option[ActivityTag]] =
    get[Option[String]]("TAG_NAME") ~ // Option because an activity can have no tags
      get[Option[String]]("TAG_DISPLAY_NAME") ~
      get[Option[String]]("TAG_VALUE") ~
      get[Option[String]]("TAG_DISPLAY_VALUE") map {
      case name ~ displayName ~ value ~ displayValue =>
        for (name <- name; value <- value) yield ActivityTag(name, displayName, TagValue(value, displayValue))
    }

  val selectActivityRender =
    """
      SELECT
        ACTIVITY.*,
        PROVIDER.SEND_EMAIL            AS PROVIDER_SEND_EMAIL,
        PROVIDER.DISPLAY_NAME          AS PROVIDER_DISPLAY_NAME,
        PROVIDER.TRANSIENT_PUSH        AS PROVIDER_TRANSIENT_PUSH,
        PROVIDER.OVERRIDE_MUTING       AS PROVIDER_OVERRIDE_MUTING,
        PROVIDER.ICON,
        PROVIDER.COLOUR,
        ACTIVITY_TAG.NAME              AS TAG_NAME,
        ACTIVITY_TAG_TYPE.DISPLAY_NAME AS TAG_DISPLAY_NAME,
        ACTIVITY_TAG.VALUE             AS TAG_VALUE,
        ACTIVITY_TAG.DISPLAY_VALUE     AS TAG_DISPLAY_VALUE,
        ACTIVITY_TYPE.DISPLAY_NAME     AS TYPE_DISPLAY_NAME
      FROM ACTIVITY
        LEFT JOIN ACTIVITY_TAG ON ACTIVITY_TAG.ACTIVITY_ID = ACTIVITY.ID
        LEFT JOIN PROVIDER ON PROVIDER.ID = ACTIVITY.PROVIDER_ID
        LEFT JOIN ACTIVITY_TYPE ON TRIM(ACTIVITY.TYPE) = ACTIVITY_TYPE.NAME
        LEFT JOIN ACTIVITY_TAG_TYPE ON ACTIVITY_TAG.NAME = ACTIVITY_TAG_TYPE.NAME
    """

  lazy val activityRenderParser: RowParser[ActivityRender] =
    activityParser ~ activityIconParser.? ~ tagParser ~ activityProviderParser ~ activityTypeParser map {
      case activity ~ icon ~ tag ~ provider ~ activityType =>
        ActivityRender(activity, icon, tag.toSeq, provider, activityType)
    }

  override def getActivityIcon(providerId: String)(implicit c: Connection): Option[ActivityIcon] =
    SQL"SELECT icon, colour FROM PROVIDER WHERE ID = $providerId"
      .as(activityIconParser.singleOpt)

  override def allProviders(implicit c: Connection): Seq[ActivityProvider] =
    SQL"""
      SELECT
        ID AS PROVIDER_ID,
        SEND_EMAIL AS PROVIDER_SEND_EMAIL,
        DISPLAY_NAME AS PROVIDER_DISPLAY_NAME,
        TRANSIENT_PUSH AS PROVIDER_TRANSIENT_PUSH,
        OVERRIDE_MUTING AS PROVIDER_OVERRIDE_MUTING
      FROM PROVIDER
    """
      .as(activityProviderParser.*)

  override def getProvider(id: String)(implicit c: Connection): Option[ActivityProvider] =
    SQL"""
      SELECT
        ID AS PROVIDER_ID,
        SEND_EMAIL AS PROVIDER_SEND_EMAIL,
        DISPLAY_NAME AS PROVIDER_DISPLAY_NAME,
        TRANSIENT_PUSH AS PROVIDER_TRANSIENT_PUSH,
        OVERRIDE_MUTING AS PROVIDER_OVERRIDE_MUTING
      FROM PROVIDER WHERE ID = $id
    """
      .as(activityProviderParser.singleOpt)

  override def countNotificationsSinceDateGroupedByPublisher(activityType: String, since: DateTime)(implicit c: Connection): Seq[PublisherActivityCount] = {
    SQL"""
      SELECT ID, NAME,
        (SELECT COUNT(*)
         FROM ACTIVITY
         WHERE PUBLISHER_ID = PUBLISHER.ID AND PUBLISHED_AT >= $since AND TYPE = $activityType AND SHOULD_NOTIFY = 1) AS COUNT
      FROM PUBLISHER
      """
      .as(publisherActivityCountParser.*)
  }

  override def updateAudienceCount(activityId: String, audienceSize: AudienceSize)(implicit c: Connection): Int = {
    SQL"""
      UPDATE ACTIVITY SET audience_size=${audienceSize.toOption}
      WHERE id=$activityId
    """.executeUpdate()
  }

  override def getAudienceSizes(ids: Seq[String])(implicit c: Connection): Map[String, AudienceSize] = {
    ids.grouped(1000).flatMap { group =>
      SQL"SELECT ID, AUDIENCE_SIZE FROM ACTIVITY WHERE ID IN ($group)"
        .as((get[String]("ID") ~ get[Option[Int]]("AUDIENCE_SIZE") map {
          case id ~ audienceSize => (id, AudienceSize.fromOption(audienceSize))
        }).*)
    }.toMap
  }

  override def getSentCounts(ids: Seq[String])(implicit c: Connection): Map[String, Int] = {
    ids.grouped(1000).flatMap { group =>
      SQL"SELECT ID, SENT_COUNT FROM ACTIVITY WHERE ID IN ($group)"
        .as((get[String]("ID") ~ get[Int]("SENT_COUNT") map {
          case id ~ sentCount => (id, sentCount)
        }).*)
    }.toMap
  }

  override def setSentCount(id: String, count: Int)(implicit c: Connection): Unit = {
    SQL"UPDATE ACTIVITY SET SENT_COUNT = $count WHERE ID = $id"
      .execute()
  }

  private lazy val activityIconParser: RowParser[ActivityIcon] =
    get[String]("ICON") ~
      get[Option[String]]("COLOUR") map {
      case icon ~ colour => ActivityIcon(icon, colour)
    }

  private lazy val activityProviderParser: RowParser[ActivityProvider] =
    get[String]("PROVIDER_ID") ~
      get[Option[Boolean]]("PROVIDER_SEND_EMAIL") ~
      get[Option[String]]("PROVIDER_DISPLAY_NAME") ~
      get[Boolean]("PROVIDER_TRANSIENT_PUSH") ~
      get[Boolean]("PROVIDER_OVERRIDE_MUTING") map {
      case id ~ sendEmail ~ displayName ~ transientPush ~ overrideMuting => ActivityProvider(id, sendEmail.getOrElse(false), displayName, transientPush, overrideMuting)
    }

  private lazy val activityTypeParser: RowParser[ActivityType] =
    get[String]("TYPE") ~
      get[Option[String]]("TYPE_DISPLAY_NAME") map {
      case name ~ displayName => ActivityType(name, displayName)
    }

  private lazy val publisherActivityCountParser: RowParser[PublisherActivityCount] =
    get[String]("ID") ~
      get[String]("NAME") ~
      get[Int]("COUNT") map {
      case id ~ name ~ count => PublisherActivityCount(id, name, count)
    }
}

