package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.PublishCategory

@ImplementedBy(classOf[PublishCategoryDaoImpl])
trait PublishCategoryDao {

  def all()(implicit c: Connection): Seq[PublishCategory]

  def saveNewsCategories(newsItemId: String, categoryIds: Seq[String])(implicit c: Connection): Unit

  def getNewsCategories(newsItemId: String)(implicit c: Connection): Seq[PublishCategory]

  def saveNotificationCategories(newsItemId: String, categoryIds: Seq[String])(implicit c: Connection): Unit

  def getNotificationCategories(newsItemId: String)(implicit c: Connection): Seq[PublishCategory]

}

@Singleton
class PublishCategoryDaoImpl extends PublishCategoryDao {

  val parser = str("id") ~ str("name") map { case id ~ name => PublishCategory(id, name) }

  override def all()(implicit c: Connection): Seq[PublishCategory] =
    SQL"SELECT ID, NAME FROM PUBLISH_CATEGORY ORDER BY NAME"
      .as(parser.*)

  override def saveNewsCategories(newsItemId: String, categoryIds: Seq[String])(implicit c: Connection) =
    categoryIds.foreach { categoryId =>
      SQL"INSERT INTO NEWS_ITEM_CATEGORY (NEWS_ITEM_ID, PUBLISH_CATEGORY_ID) VALUES ($newsItemId, $categoryId)".execute()
    }

  override def getNewsCategories(newsItemId: String)(implicit c: Connection): Seq[PublishCategory] =
    SQL"SELECT ID, NAME FROM PUBLISH_CATEGORY WHERE ID IN (SELECT ID FROM NEWS_ITEM_CATEGORY WHERE NEWS_ITEM_ID = $newsItemId)"
      .as(parser.*)

  override def saveNotificationCategories(notificationId: String, categoryIds: Seq[String])(implicit c: Connection) =
    categoryIds.foreach { categoryId =>
      SQL"INSERT INTO NOTIFICATION_CATEGORY (NOTIFICATION_ID, PUBLISH_CATEGORY_ID) VALUES ($notificationId, $categoryId)".execute()
    }

  override def getNotificationCategories(notificationId: String)(implicit c: Connection): Seq[PublishCategory] =
    SQL"SELECT ID, NAME FROM PUBLISH_CATEGORY WHERE ID IN (SELECT ID FROM NOTIFICATION_CATEGORY WHERE NOTIFICATION_ID = $notificationId)"
      .as(parser.*)

}
