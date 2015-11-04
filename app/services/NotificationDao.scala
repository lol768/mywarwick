package services

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.{IncomingNotification, Notification}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[NotificationDaoImpl])
trait NotificationDao {
  def save(incomingNotification: IncomingNotification,
    replaces: Seq[String])(implicit connection: Connection): String

  def getNotificationById(id: String): Option[Notification] =
    getNotificationsByIds(Seq(id)).headOption

  def getNotificationsByIds(ids: Seq[String]): Seq[Notification]

}

class NotificationDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends NotificationDao {

  private def notificationParser: RowParser[Notification] = {
    get[String]("id") ~
      get[String]("provider_id") ~
      get[String]("type") ~
      get[String]("title") ~
      get[String]("text") ~
      get[String]("replaced_by_id") ~
      get[DateTime]("created_at") map {
      case id ~ providerId ~ notificationType ~ title ~ text ~ replacedById ~ createdAt =>
        Notification(id, providerId, notificationType, title, text, replacedById, createdAt)
    }
  }

  override def save(incomingNotification: IncomingNotification, replaces: Seq[String])(implicit c: Connection): String = {
    import incomingNotification._
    val id = UUID.randomUUID().toString
    val now = new DateTime()

    SQL("INSERT INTO notification(id, provider_id, type, title, text, generated_at, created_at) VALUES({id}, {providerId}, {type}, {title}, {text}, {generatedAt}, {createdAt})")
      .on(
        'id -> id,
        'providerId -> providerId,
        'type -> notificationType,
        'title -> title,
        'text -> text,
        'generatedAt -> generatedAt.getOrElse(now),
        'createdAt -> now
      )
      .execute()

    updateReplacedNotification(id, replaces)

    id
  }

  def updateReplacedNotification(replacedById: String, replaces: Seq[String]) = {
    db.withConnection { implicit c =>
      replaces.grouped(1000).foreach { group =>
        SQL("UPDATE notification SET replaced_by_id = {replacedById} WHERE id IN ({replaces})")
          .on('replacedById -> replacedById,
            'replaces -> group)
          .execute()
      }
    }
  }

  def getNotificationsByIds(ids: Seq[String]): Seq[Notification] = {
    db.withConnection { implicit c =>
      SQL(s"SELECT * FROM notification WHERE id IN ({ids})")
        .on('ids -> ids)
        .as(notificationParser.*)
    }
  }
}
