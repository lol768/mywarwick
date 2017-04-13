package services.dao

import anorm.SqlParser._
import anorm._
import helpers.OneStartAppPerSuite
import helpers.BaseSpec
import warwick.sso.Usercode

class UserNewsCategoryDaoTest extends BaseSpec with OneStartAppPerSuite {

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

    "get users with any categories selected" in transaction { implicit c =>
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('cusab', 'category-a')".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('cusab', 'category-b')".execute()

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('cusabc', 'category-a')".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('cusabc', 'category-b')".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('cusabc', 'category-c')".execute()

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('cusac', 'category-a')".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES ('cusac', 'category-c')".execute()

      dao.getRecipientsOfNewsInCategories(Seq("category-a")).map(_.string) must contain allOf("cusab", "cusabc", "cusac")
      dao.getRecipientsOfNewsInCategories(Seq("category-b")).map(_.string) must contain allOf("cusab", "cusabc")
      dao.getRecipientsOfNewsInCategories(Seq("category-c")).map(_.string) must contain allOf("cusabc", "cusac")

      dao.getRecipientsOfNewsInCategories(Seq("category-a", "category-b")).map(_.string) must contain allOf("cusab", "cusabc")
      dao.getRecipientsOfNewsInCategories(Seq("category-a", "category-c")).map(_.string) must contain allOf("cusabc", "cusac")
      dao.getRecipientsOfNewsInCategories(Seq("category-a", "category-b", "category-c")).map(_.string) must contain allOf("cusab", "cusabc", "cusac")

      dao.getRecipientsOfNewsInCategories(Nil).map(_.string) must be(empty)
    }

  }

}
