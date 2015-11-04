package services

import java.util.UUID

import anorm.JodaParameterMetaData._
import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.{IncomingNotification, Notification}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[NotificationDaoImpl])
trait NotificationDao {
  def save(incomingNotification: IncomingNotification,
           replaces: Seq[String]): String

  def updateReplacedNotification(id: String, replacedById: String)

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

  override def save(incomingNotification: IncomingNotification, replaces: Seq[String]): String = {
    import incomingNotification._
    val id = UUID.randomUUID().toString
    val now = new DateTime()
    db.withConnection { implicit c =>
      SQL("INSERT INTO notification(id, provider_id, type, title, text, generated_at, created_at) VALUES({id}, {providerId}, {type}, {title}, {text}, {generatedAt}, {createdAt})")
        .on(
          'id -> id,
          'providerId -> providerId,
          'type -> notificationType,
          'title -> title,
          'text -> text,
          'generatedAt -> now,
          'createdAt -> now
        )
        .execute()
    }

    replaces.foreach(replacesId => {
      updateReplacedNotification(replacesId, id)
    })

    id
  }

  override def updateReplacedNotification(replacesId: String, replacedById: String) = {
    db.withConnection { implicit c =>
      SQL("UPDATE notification SET replaced_by_id = {replacedById} WHERE id = {replacesId}")
        .on('replacedById -> replacedById,
          'replacesId -> replacesId)
        .execute()
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
