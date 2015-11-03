package services

import com.google.inject.{ImplementedBy, Inject}


@ImplementedBy(classOf[NotificationServiceImpl])
trait NotificationService {
//  def getNotificationById(id: String): Option[Notification]

  def save(providerId: String,
           notificationType: String,
           title: String,
           text: String,
           replaces: Option[String]): String
}

class NotificationServiceImpl @Inject()(notificationDao: NotificationDao) extends NotificationService {
//  override def getNotificationById(id: String): Option[Notification] = notificationDao.getNotificationById(id)

  def save(providerId: String, notificationType: String, title: String, text: String, replaces: Option[String]): String = {
    notificationDao.save(providerId, notificationType, title, text, replaces)
  }
}

