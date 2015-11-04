package services

import com.google.inject.{ImplementedBy, Inject}
import models.IncomingNotification
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[NotificationCreationDaoImpl])
trait NotificationCreationDao {

  def createNotification(notification: IncomingNotification, replaces: Seq[String]): String

}

class NotificationCreationDaoImpl @Inject()(
  @NamedDatabase("default") val db: Database,
  notificationDao: NotificationDao,
  notificationScopeDao: NotificationScopeDao
) extends NotificationCreationDao {

  override def createNotification(incomingNotification: IncomingNotification, replaceIds: Seq[String]): String = {

    db.withTransaction { implicit c =>
      import incomingNotification._
      val notification = notificationDao.save(incomingNotification, replaceIds)(c)

      scopes.foreach {
        case (name, value) => notificationScopeDao.save(notification, name, value)(c)
      }

      notification
    }

  }
}
