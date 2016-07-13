package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.PublishedNotificationSave
import play.api.db.{Database, NamedDatabase}
import services.dao.PublishedNotificationsDao

@ImplementedBy(classOf[PublishedNotificationsServiceImpl])
trait PublishedNotificationsService {

  def save(publishedNotification: PublishedNotificationSave): Unit

}

@Singleton
class PublishedNotificationsServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: PublishedNotificationsDao
) extends PublishedNotificationsService {

  override def save(publishedNotification: PublishedNotificationSave) = db.withTransaction(implicit c => dao.save(publishedNotification))

}
