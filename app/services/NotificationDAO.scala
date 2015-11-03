package services

import java.util.UUID

import anorm.JodaParameterMetaData._
import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.{DBConversions, Notification}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[NotificationDaoImpl])
trait NotificationDao {
  def save(provider_id: String,
           notification_type: String,
           title: String,
           text: String,
           replaces: Seq[String]): String

  def updateReplacedNotification(id: String, replacedById: String)

    def getNotificationById(id: String): Option[Notification] =
      getNotificationsByIds(Seq(id)).headOption

    def getNotificationsByIds(ids: Seq[String]): Seq[Notification]

}

class NotificationDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends NotificationDao with DBConversions {

  private def notificationParser: RowParser[Notification] = {
    get[String]("id") ~
      get[String]("provider_id") ~
      get[String]("notification_type") ~
      get[String]("title") ~
      get[String]("text") ~
      get[String]("replacedby_id") ~
      get[DateTime]("created_at") map {
      case id ~ providerId ~ notificationType ~ title ~ text ~ replacedById ~ createdAt =>
        Notification(id, providerId, notificationType, title, text, replacedById, createdAt)
    }
  }

  override def save(providerId: String, notificationType: String, title: String, text: String, replaces: Seq[String]): String = {
    val id = UUID.randomUUID().toString
    val now = new DateTime()
    db.withConnection { implicit c =>
      SQL("INSERT INTO notification(id, provider_id, notification_type, title, text, replacedby_id, created_at) VALUES({id}, {providerId}, {notificationType}, {title}, {text}, null, {createdAt})")
        .on('id -> id,
          'providerId -> providerId,
          'notificationType -> notificationType,
          'title -> title,
          'text -> text,
          'createdAt -> now)
        .execute()
    }

    replaces.foreach(replacesId => {
      updateReplacedNotification(replacesId, id)
    })

    id
  }

  override def updateReplacedNotification(replacesId: String, replacedById: String) = {
    db.withConnection { implicit c =>
      SQL("UPDATE notification SET replacedby_id = {replacedById} WHERE id = {replacesId}")
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
