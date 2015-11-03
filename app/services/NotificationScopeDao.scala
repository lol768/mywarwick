package services

import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.DBConversions
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[NotificationScopeDaoImpl])
trait NotificationScopeDao {

  def save(notificationId: String, name: String): String

  def getNotificationsByScope(scopes: Seq[String], providerId: String): Seq[String]

}

class NotificationScopeDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends NotificationScopeDao with DBConversions {

  override def save(notificationId: String, name: String): String = {
    db.withConnection { implicit c =>
      val scopeId = UUID.randomUUID().toString

      SQL("INSERT INTO NOTIFICATION_SCOPE (NOTIFICATION_ID, SCOPE_ID, SCOPE_TYPE, SCOPE_NAME) VALUES ({notificationId}, {scopeId}, {scopeType}, {scopeName})")
        .on('notificationId -> notificationId,
          'scopeId -> scopeId,
          'scopeType -> name,
          'scopeName -> name)
        .execute()

      scopeId
    }
  }

  override def getNotificationsByScope(scopes: Seq[String], providerId: String): Seq[String] = {
    db.withConnection { implicit c =>
      SQL("SELECT NOTIFICATION_ID FROM NOTIFICATION_SCOPE JOIN NOTIFICATION ON NOTIFICATION.ID = NOTIFICATION_SCOPE.NOTIFICATION_ID WHERE SCOPE_NAME IN ({scopes}) AND PROVIDER_ID = {providerId} GROUP BY NOTIFICATION_ID HAVING COUNT(*) = {count}")
        .on('scopes -> scopes, 'count -> scopes.length, 'providerId -> providerId)
        .as(str("NOTIFICATION_ID").*)
    }
  }

}