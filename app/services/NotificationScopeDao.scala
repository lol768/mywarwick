package services

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[NotificationScopeDaoImpl])
trait NotificationScopeDao {

  def save(notificationId: String, name: String, value: String)(implicit connection: Connection): String

  def getNotificationsByScope(scopes: Map[String, String], providerId: String): Seq[String]

}

class NotificationScopeDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends NotificationScopeDao {

  override def save(notificationId: String, name: String, value: String)(implicit c: Connection): String = {
    val scopeId = UUID.randomUUID().toString

    throw new Exception()

    SQL("INSERT INTO NOTIFICATION_SCOPE (NOTIFICATION_ID, ID, NAME, VALUE, CREATED_AT) VALUES ({notificationId}, {id}, {name}, {value}, {createdAt})")
      .on(
        'notificationId -> notificationId,
        'id -> scopeId,
        'name -> name,
        'value -> value,
        'createdAt -> DateTime.now()
      )
      .execute()

    scopeId
  }

  override def getNotificationsByScope(scopes: Map[String, String], providerId: String): Seq[String] = {
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

      SQL("SELECT NOTIFICATION_ID FROM NOTIFICATION_SCOPE JOIN NOTIFICATION ON NOTIFICATION.ID = NOTIFICATION_SCOPE.NOTIFICATION_ID WHERE (NAME, VALUE) IN ({scopes}) AND PROVIDER_ID = {providerId} GROUP BY NOTIFICATION_ID HAVING COUNT(*) = {count}")
        .on(
          'scopes -> scopesParam,
          'providerId -> providerId,
          'count -> scopes.size
        )
        .as(str("NOTIFICATION_ID").*)
      */

      // TODO fix
      SQL("SELECT NOTIFICATION_ID FROM NOTIFICATION_SCOPE JOIN NOTIFICATION ON NOTIFICATION.ID = NOTIFICATION_SCOPE.NOTIFICATION_ID WHERE (VALUE) IN ({scopes}) AND PROVIDER_ID = {providerId} GROUP BY NOTIFICATION_ID HAVING COUNT(*) = {count}")
        .on(
          'scopes -> scopes.values.toSeq,
          'providerId -> providerId,
          'count -> scopes.size
        )
        .as(str("NOTIFICATION_ID").*)
    }
  }

}