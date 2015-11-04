package services

import com.google.inject.{ImplementedBy, Inject}
import models.{IncomingNotification, Notification}


@ImplementedBy(classOf[NotificationServiceImpl])
trait NotificationService {
  def getNotificationById(id: String): Option[Notification]

  def save(incomingNotification: IncomingNotification): String
}

class NotificationServiceImpl @Inject()(notificationDao: NotificationDao, notificationScopeDao: NotificationScopeDao) extends NotificationService {
  override def getNotificationById(id: String): Option[Notification] = notificationDao.getNotificationById(id)

  //  def save(providerId: String, notificationType: String, title: String, text: String, scopes: Seq[String], replace: Boolean): String = {
  def save(incomingNotification: IncomingNotification): String = {
    import incomingNotification._
    val replaceIds = replace match {
      case true => notificationScopeDao.getNotificationsByScope(scopes, providerId)
      case _ => Nil
    }

    val notification = notificationDao.save(incomingNotification, replaceIds)

    scopes.foreach(name => notificationScopeDao.save(notification, name))

    notification

  }
}

