package services

import com.google.inject.{ImplementedBy, Inject}
import models.{IncomingNotification, Notification}


@ImplementedBy(classOf[NotificationServiceImpl])
trait NotificationService {
  def getNotificationById(id: String): Option[Notification]

  def save(incomingNotification: IncomingNotification): String
}

class NotificationServiceImpl @Inject()(
  notificationCreationDao: NotificationCreationDao,
  notificationDao: NotificationDao,
  notificationScopeDao: NotificationScopeDao
) extends NotificationService {
  override def getNotificationById(id: String): Option[Notification] = notificationDao.getNotificationById(id)

  def save(incomingNotification: IncomingNotification): String = {
    import incomingNotification._
    val replaceIds = notificationScopeDao.getNotificationsByScope(replace, providerId)

    notificationCreationDao.createNotification(incomingNotification, replaceIds)

  }
}

