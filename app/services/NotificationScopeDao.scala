package services

import java.util.UUID

import anorm.JodaParameterMetaData._
import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[NotificationScopeDaoImpl])
trait NotificationScopeDao {

  def save(notificationId: String, name: String): String

  def getNotificationsByScope(scopes: Seq[String], providerId: String): Seq[String]

}

class NotificationScopeDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends NotificationScopeDao {

  override def save(notificationId: String, name: String): String = {
    db.withConnection { implicit c =>
      val scopeId = UUID.randomUUID().toString

      SQL("INSERT INTO NOTIFICATION_SCOPE (NOTIFICATION_ID, ID, NAME, VALUE, CREATED_AT) VALUES ({notificationId}, {id}, {name}, {value}, {createdAt})")
        .on(
          'notificationId -> notificationId,
          'id -> scopeId,
          'name -> name,
          'value -> name,
          'createdAt -> DateTime.now()
        )
        .execute()

      scopeId
    }
  }

  override def getNotificationsByScope(scopes: Seq[String], providerId: String): Seq[String] = {
    db.withConnection { implicit c =>
      SQL("SELECT NOTIFICATION_ID FROM NOTIFICATION_SCOPE JOIN NOTIFICATION ON NOTIFICATION.ID = NOTIFICATION_SCOPE.NOTIFICATION_ID WHERE VALUE IN ({scopes}) AND PROVIDER_ID = {providerId} GROUP BY NOTIFICATION_ID HAVING COUNT(*) = {count}")
        .on('scopes -> scopes, 'count -> scopes.length, 'providerId -> providerId)
        .as(str("NOTIFICATION_ID").*)
    }
  }

}