package services.dao

import anorm.SqlParser._
import anorm._
import helpers.OneStartAppPerSuite
import models.NewsCategory
import org.scalatestplus.play.PlaySpec

class NewsCategoryDaoTest extends PlaySpec with OneStartAppPerSuite {

  val dao = get[NewsCategoryDao]

  transaction(rollback = false) { implicit c =>
    SQL"DELETE FROM NEWS_CATEGORY".execute()

    SQL"INSERT INTO NEWS_CATEGORY (ID, NAME) VALUES ('category-a', 'Category A'), ('category-b', 'Category B'), ('category-c', 'Category C')"
      .execute()
  }

  "NewsCategoryDao" should {

    "get all categories" in transaction { implicit c =>
      dao.all() mustBe Seq(
        NewsCategory("category-a", "Category A"),
        NewsCategory("category-b", "Category B"),
        NewsCategory("category-c", "Category C")
      )
    }

    "get news item categories" in transaction { implicit c =>
      SQL"INSERT INTO NEWS_ITEM (ID, TITLE, TEXT, CREATED_AT, PUBLISH_DATE, PUBLISHER_ID, CREATED_BY) VALUES ('news-item', 'Something happened', 'It is now over', SYSDATE, SYSDATE, 'publisher', 'custard')"
        .execute()

      SQL"INSERT INTO NEWS_ITEM_CATEGORY (NEWS_ITEM_ID, NEWS_CATEGORY_ID) VALUES ('news-item', 'category-b'), ('news-item', 'category-a')"
        .execute()

      dao.getNewsCategories("news-item") mustBe Seq(
        NewsCategory("category-a", "Category A"),
        NewsCategory("category-b", "Category B")
      )
    }

    "save news item categories" in transaction { implicit c =>
      SQL"INSERT INTO NEWS_ITEM (ID, TITLE, TEXT, CREATED_AT, PUBLISH_DATE, PUBLISHER_ID, CREATED_BY) VALUES ('news-item', 'Something happened', 'It is now over', SYSDATE, SYSDATE, 'publisher', 'custard')"
        .execute()

      dao.saveNewsCategories("news-item", Seq("category-b", "category-c"))

      SQL"SELECT NEWS_CATEGORY_ID FROM NEWS_ITEM_CATEGORY WHERE NEWS_ITEM_ID = 'news-item'"
        .executeQuery()
        .as(scalar[String].*) mustBe Seq("category-b", "category-c")
    }

  }

}
