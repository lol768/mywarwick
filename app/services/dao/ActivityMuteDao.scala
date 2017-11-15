package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models._
import org.joda.time.DateTime
import play.api.libs.json.{JsArray, Json}
import warwick.anorm.converters.ColumnConversions._
import warwick.sso.Usercode

import scala.collection.mutable

@ImplementedBy(classOf[ActivityMuteDaoImpl])
trait ActivityMuteDao {

  def save(mute: ActivityMuteSave)(implicit c: Connection): String

  def expire(mute: ActivityMuteRender)(implicit c: Connection): Boolean

  def mutesForActivity(activity: Activity, recipients: Set[Usercode] = Set.empty)(implicit c: Connection): Seq[ActivityMute]

  def mutesForRecipient(recipient: Usercode)(implicit c: Connection): Seq[ActivityMuteRender]

  def deleteExpiredBefore(expiredBefore: DateTime)(implicit c: Connection): Int

  def mutesForProvider(provider: ActivityProvider)(implicit c: Connection): Seq[ActivityMute]

  def mutesCountForProvider(provider: ActivityProvider)(implicit c: Connection): Int
}

class ActivityMuteDaoImpl extends ActivityMuteDao {

  override def save(mute: ActivityMuteSave)(implicit c: Connection): String = {
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

  override def expire(mute: ActivityMuteRender)(implicit c: Connection): Boolean = {
    val now = DateTime.now
    SQL"UPDATE ACTIVITY_MUTE SET EXPIRES_AT = $now WHERE ID = ${mute.id}".execute()
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

  private lazy val activityMuteRenderParser: RowParser[ActivityMuteRender] =
    get[String]("ID") ~
      get[String]("USERCODE") ~
      get[DateTime]("CREATED_AT") ~
      get[Option[DateTime]]("EXPIRES_AT") ~
      get[Option[String]]("PROVIDER_ID") ~
      get[Option[Boolean]]("PROVIDER_SEND_EMAIL") ~
      get[Option[String]]("PROVIDER_DISPLAY_NAME") ~
      get[Option[String]]("ACTIVITY_TYPE") ~
      get[Option[String]]("ACTIVITY_TYPE_DISPLAY_NAME") ~
      get[Option[String]]("TAGS") map {
      case id ~ usercode ~ createdAt ~ expiresAt ~ providerOption ~ providerSendEmail ~ providerDisplayName ~ activityTypeOption ~ activityTypeDisplayName ~ tagString =>
        ActivityMuteRender(
          id,
          Usercode(usercode),
          createdAt,
          expiresAt,
          activityTypeOption.map(activityType => ActivityType(activityType, activityTypeDisplayName)),
          providerOption.map(provider => ActivityProvider(provider, providerSendEmail.get, providerDisplayName)),
          tagString
            .map(Json.parse(_).as[JsArray].value.map(_.as[ActivityTag]))
            .getOrElse(Nil)
        )
    }

  def mutesForRecipient(recipient: Usercode)(implicit c: Connection): Seq[ActivityMuteRender] = {
    SQL(s"""
      SELECT
        ACTIVITY_MUTE.*,
        PROVIDER.SEND_EMAIL        AS PROVIDER_SEND_EMAIL,
        PROVIDER.DISPLAY_NAME      AS PROVIDER_DISPLAY_NAME,
        ACTIVITY_TYPE.DISPLAY_NAME AS ACTIVITY_TYPE_DISPLAY_NAME
      FROM ACTIVITY_MUTE
        LEFT JOIN PROVIDER ON ACTIVITY_MUTE.PROVIDER_ID = PROVIDER.ID
        LEFT JOIN ACTIVITY_TYPE ON ACTIVITY_MUTE.ACTIVITY_TYPE = ACTIVITY_TYPE.NAME
        WHERE USERCODE = {usercode}
    """)
      .on("usercode" -> recipient.string)
      .as(activityMuteRenderParser.*)
  }

  override def deleteExpiredBefore(expiredBefore: DateTime)(implicit c: Connection): Int = {
    SQL(s"DELETE FROM ACTIVITY_MUTE WHERE EXPIRES_AT < {expiredBefore}")
      .on("expiredBefore" -> expiredBefore)
      .executeUpdate()
  }

  override def mutesForProvider(provider: ActivityProvider)(implicit c: Connection) = {
    SQL(s"select * from ACTIVITY_MUTE where PROVIDER_ID = {PROVIDER_ID}")
      .on("PROVIDER_ID" -> provider.id)
      .as(activityMuteParser.*)
  }

  override def mutesCountForProvider(provider: ActivityProvider)(implicit c: Connection) = {
    SQL(s"select count(*) from ACTIVITY_MUTE where PROVIDER_ID = {PROVIDER_ID}")
      .on("PROVIDER_ID" -> provider.id)
      .as(scalar[Int].single)
  }
}