package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.{Activity, ActivityMute, ActivityTag}
import org.joda.time.DateTime
import play.api.libs.json.{JsArray, Json}
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

import scala.collection.mutable

@ImplementedBy(classOf[ActivityMuteDaoImpl])
trait ActivityMuteDao {

  def save(mute: ActivityMute)(implicit c: Connection): String

  def mutesForActivity(activity: Activity, recipients: Set[Usercode] = Set.empty)(implicit c: Connection): Seq[ActivityMute]

  def deleteExpiredBefore(expiredBefore: DateTime)(implicit c: Connection): Int

}

class ActivityMuteDaoImpl extends ActivityMuteDao {

  override def save(mute: ActivityMute)(implicit c: Connection): String = {
    import mute._
    val id = UUID.randomUUID().toString
    val now = DateTime.now
    val tagString = JsArray(tags.map(Json.toJson[ActivityTag])).toString()
    val expiresAtOrNull: DateTime = expiresAt.orNull

    SQL"""
      INSERT INTO ACTIVITY_MUTE (id, usercode, created_at, expires_at, activity_type, provider_id, tags)
      VALUES ($id, ${usercode.string}, $now, $expiresAtOrNull, $activityType, $providerId, $tagString)
    """
      .execute()

    id
  }

  private lazy val activityMuteParser: RowParser[ActivityMute] =
    get[String]("USERCODE") ~
      get[DateTime]("CREATED_AT") ~
      get[Option[DateTime]]("EXPIRES_AT") ~
      get[Option[String]]("ACTIVITY_TYPE") ~
      get[Option[String]]("PROVIDER_ID") ~
      get[Option[String]]("TAGS") map {
      case usercode ~ createdAt ~ expiresAt ~ activityType ~ providerId ~ tagString =>
        ActivityMute(
          Usercode(usercode),
          createdAt,
          expiresAt,
          activityType,
          providerId,
          tagString
            .map(Json.parse(_).as[JsArray].value.map(_.as[ActivityTag]))
            .getOrElse(Nil)
        )
    }

  override def mutesForActivity(activity: Activity, recipients: Set[Usercode] = Set.empty)(implicit c: Connection): Seq[ActivityMute] = {
    val q = new StringBuilder(s"""
      SELECT * FROM ACTIVITY_MUTE
        WHERE (ACTIVITY_TYPE IS NULL OR ACTIVITY_TYPE = {activityType})
        AND (PROVIDER_ID IS NULL OR PROVIDER_ID = {providerId})
    """)
    val args = mutable.ArrayBuffer[NamedParameter]("activityType" -> activity.`type`, "providerId" -> activity.providerId)

    if (recipients.nonEmpty) {
      assert(recipients.size <= 1000)
      q.append(s"""
        AND USERCODE IN ({usercodes})
      """)
      args.append(NamedParameter.string("usercodes", recipients.map(_.string)))
    }

    SQL(q.mkString)
      .on(args:_*)
      .as(activityMuteParser.*)
  }

  override def deleteExpiredBefore(expiredBefore: DateTime)(implicit c: Connection): Int = {
    SQL(s"DELETE FROM ACTIVITY_MUTE WHERE EXPIRES_AT < {expiredBefore}")
      .on("expiredBefore" -> expiredBefore)
      .executeUpdate()
  }

}