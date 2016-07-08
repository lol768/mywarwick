package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.NewsCategory

@ImplementedBy(classOf[NewsCategoryDaoImpl])
trait NewsCategoryDao {

  def all()(implicit c: Connection): Seq[NewsCategory]

  def saveNewsCategories(newsItemId: String, categoryIds: Seq[String])(implicit c: Connection): Unit

  def getNewsCategories(newsItemId: String)(implicit c: Connection): Seq[NewsCategory]

  def deleteNewsCategories(newsItemId: String)(implicit c: Connection): Unit

}

@Singleton
class NewsCategoryDaoImpl extends NewsCategoryDao {

  val parser = str("id") ~ str("name") map { case id ~ name => NewsCategory(id, name) }

  override def all()(implicit c: Connection): Seq[NewsCategory] =
    SQL"SELECT ID, NAME FROM NEWS_CATEGORY ORDER BY NAME"
      .as(parser.*)

  override def saveNewsCategories(newsItemId: String, categoryIds: Seq[String])(implicit c: Connection) =
    categoryIds.foreach { categoryId =>
      SQL"INSERT INTO NEWS_ITEM_CATEGORY (NEWS_ITEM_ID, NEWS_CATEGORY_ID) VALUES ($newsItemId, $categoryId)".execute()
    }

  override def getNewsCategories(newsItemId: String)(implicit c: Connection): Seq[NewsCategory] =
    SQL"SELECT ID, NAME FROM NEWS_CATEGORY WHERE ID IN (SELECT NEWS_CATEGORY_ID FROM NEWS_ITEM_CATEGORY WHERE NEWS_ITEM_ID = $newsItemId) ORDER BY NAME"
      .as(parser.*)

  override def deleteNewsCategories(newsItemId: String)(implicit c: Connection) =
    SQL"DELETE FROM NEWS_ITEM_CATEGORY WHERE NEWS_ITEM_ID=$newsItemId".execute()

}
