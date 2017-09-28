package services.dao

import anorm.SqlParser._
import anorm._
import helpers.OneStartAppPerSuite
import helpers.BaseSpec
import warwick.sso.Usercode

class UserNewsCategoryDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val dao = get[UserNewsCategoryDao]

  val allCategories = Seq(("category-a", "Category A"), ("category-b", "Category B"), ("category-c", "Category C"))

  transaction(rollback = false) { implicit c =>
    SQL"DELETE FROM NEWS_CATEGORY".execute()

    allCategories.foreach { case (id, name) =>
      SQL"INSERT INTO NEWS_CATEGORY (ID, NAME) VALUES ($id, $name)"
        .execute()
    }

  }

  val custard = Usercode("custard")

  "UserNewsCategoryDao" should {

    "get subscribed categories" in transaction { implicit c =>
      dao.getSubscribedCategories(custard) mustBe allCategories.map { case (id, _) => id }

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('custard', 'category-a', 0)"
        .execute()

      dao.getSubscribedCategories(custard) mustBe Seq("category-b", "category-c")

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('custard', 'category-b', 1)"
        .execute()

      dao.getSubscribedCategories(custard) mustBe Seq("category-b", "category-c")
    }

    "set subscribed categories" in transaction { implicit c =>
      val (selected, unselected) = allCategories.map { case (id, _) => id }.partition(id => id == "category-a" || id == "category-b")

      dao.setSubscribedCategories(custard, selected)

      SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = 'custard' AND SELECTED = 1"
        .executeQuery()
        .as(scalar[String].*) mustBe selected

      SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = 'custard' AND SELECTED = 0"
        .executeQuery()
        .as(scalar[String].*) mustBe unselected

      val (selected2, unselected2) = allCategories.map { case (id, _) => id }.partition(id => id == "category-c")

      dao.setSubscribedCategories(custard, selected2)

      SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = 'custard' AND SELECTED = 1"
        .executeQuery()
        .as(scalar[String].*) mustBe selected2

      SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = 'custard' AND SELECTED = 0"
        .executeQuery()
        .as(scalar[String].*) mustBe unselected2
    }

    "get users with any categories selected" in transaction { implicit c =>
      SQL"INSERT INTO USER_PREFERENCE (USERCODE) VALUES ('cusab'), ('cusabc'), ('cusac')".execute()

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('cusab', 'category-a', 0)".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('cusab', 'category-b', 0)".execute()

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('cusabc', 'category-a', 0)".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('cusabc', 'category-b', 0)".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('cusabc', 'category-c', 0)".execute()

      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('cusac', 'category-a', 0)".execute()
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES ('cusac', 'category-c', 0)".execute()

      dao.getRecipientsOfNewsInCategories(Seq("category-a")).map(_.string) mustBe empty
      dao.getRecipientsOfNewsInCategories(Seq("category-b")).map(_.string) mustBe Set("cusac")
      dao.getRecipientsOfNewsInCategories(Seq("category-c")).map(_.string) mustBe Set("cusab")

      dao.getRecipientsOfNewsInCategories(Seq("category-a", "category-b")).map(_.string) mustBe Set("cusac")
      dao.getRecipientsOfNewsInCategories(Seq("category-a", "category-c")).map(_.string) mustBe Set("cusab")
      dao.getRecipientsOfNewsInCategories(Seq("category-a", "category-b", "category-c")).map(_.string) must contain only("cusac", "cusab")

      dao.getRecipientsOfNewsInCategories(Nil).map(_.string) must be(empty)
    }

  }

}
