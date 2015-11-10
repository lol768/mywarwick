package services

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityScopeDaoImpl])
trait ActivityScopeDao {

  def save(activityId: String, name: String, value: String)(implicit connection: Connection): String

  def getActivitiesByScope(scopes: Map[String, String], providerId: String): Seq[String]

}

class ActivityScopeDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends ActivityScopeDao {

  override def save(activityId: String, name: String, value: String)(implicit c: Connection): String = {
    val scopeId = UUID.randomUUID().toString

    SQL("INSERT INTO ACTIVITY_SCOPE (ACTIVITY_ID, ID, NAME, VALUE, CREATED_AT) VALUES ({activityId}, {id}, {name}, {value}, {createdAt})")
      .on(
        'activityId -> activityId,
        'id -> scopeId,
        'name -> name,
        'value -> value,
        'createdAt -> DateTime.now()
      )
      .execute()

    scopeId
  }

  override def getActivitiesByScope(scopes: Map[String, String], providerId: String): Seq[String] = {
    db.withConnection { implicit c =>
      /*
      val scopesParam = SeqParameter(
        seq = scopes.map { case (name, value) =>
            // s"$name, $value"
           Vector(name, value)
        }.toSeq,
        pre = "(",
        post = ")"
      )

      SQL("SELECT ACTIVITY_ID FROM ACTIVITY_SCOPE JOIN ACTIVITY ON ACTIVITY.ID = ACTIVITY_SCOPE.ACTIVITY_ID WHERE (NAME, VALUE) IN ({scopes}) AND PROVIDER_ID = {providerId} GROUP BY ACTIVITY_ID HAVING COUNT(*) = {count}")
        .on(
          'scopes -> scopesParam,
          'providerId -> providerId,
          'count -> scopes.size
        )
        .as(str("ACTIVITY_ID").*)
      */

      // TODO fix
      SQL("SELECT ACTIVITY_ID FROM ACTIVITY_SCOPE JOIN ACTIVITY ON ACTIVITY.ID = ACTIVITY_SCOPE.ACTIVITY_ID WHERE (VALUE) IN ({scopes}) AND PROVIDER_ID = {providerId} GROUP BY ACTIVITY_ID HAVING COUNT(*) = {count}")
        .on(
          'scopes -> scopes.values.toSeq,
          'providerId -> providerId,
          'count -> scopes.size
        )
        .as(str("ACTIVITY_ID").*)
    }
  }

}