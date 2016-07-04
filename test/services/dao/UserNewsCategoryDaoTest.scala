package services.dao

import anorm.SqlParser._
import anorm._
import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec
import warwick.sso.Usercode

class UserNewsCategoryDaoTest extends PlaySpec with OneStartAppPerSuite {

  val dao = get[UserNewsCategoryDao]

  transaction(rollback = false) { implicit c =>
    SQL"INSERT INTO NEWS_CATEGORY (ID, NAME) VALUES ('category-a', 'Category A'), ('category-b', 'Category B'), ('category-c', 'Category C')"
      .execute()
  }

  val custard = Usercode("custard")

  "UserNewsCategoryDao" should {

    "get subscribed categories" in transaction { implicit c =>
      dao.getSubscribedCategories(custard) mustBe empty

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('custard', 'category-a')"
        .execute()

      dao.getSubscribedCategories(custard) mustBe Seq("category-a")
    }

    "set subscribed categories" in transaction { implicit c =>
      dao.setSubscribedCategories(custard, Seq("category-a", "category-b"))

      SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = 'custard'"
        .executeQuery()
        .as(scalar[String].*) mustBe Seq("category-a", "category-b")

      dao.setSubscribedCategories(custard, Seq("category-c"))

      SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = 'custard'"
        .executeQuery()
        .as(scalar[String].*) mustBe Seq("category-c")
    }

  }

}
