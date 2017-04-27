package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject, Singleton}
import warwick.sso.Usercode

@ImplementedBy(classOf[UserNewsCategoryDaoImpl])
trait UserNewsCategoryDao {

  def getSubscribedCategories(usercode: Usercode)(implicit c: Connection): Seq[String]

  def setSubscribedCategories(usercode: Usercode, categoryIds: Seq[String])(implicit c: Connection): Unit

  def getRecipientsOfNewsInCategories(categoryIds: Seq[String])(implicit c: Connection): Seq[Usercode]

}

@Singleton
class UserNewsCategoryDaoImpl @Inject()(
  newsCategoryDao: NewsCategoryDao,
  userPreferencesDao: UserPreferencesDao
) extends UserNewsCategoryDao {

  override def getSubscribedCategories(usercode: Usercode)(implicit c: Connection): Seq[String] =
    newsCategoryDao.all().map(_.id).diff(
      SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = ${usercode.string} AND SELECTED = 0"
        .executeQuery()
        .as(scalar[String].*)
    )


  override def setSubscribedCategories(usercode: Usercode, categoryIds: Seq[String])(implicit c: Connection): Unit = {
    SQL"DELETE FROM USER_NEWS_CATEGORY WHERE USERCODE = ${usercode.string}"
      .execute()

    newsCategoryDao.all().map(_.id).foreach { categoryId =>
      val selected = if (categoryIds.contains(categoryId)) 1 else 0
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID, SELECTED) VALUES (${usercode.string}, $categoryId, $selected)"
        .execute()
    }
  }

  override def getRecipientsOfNewsInCategories(categoryIds: Seq[String])(implicit c: Connection): List[Usercode] = {
    if (categoryIds.isEmpty) {
      List()
    } else {
      userPreferencesDao.allInitialisedUsers().diff(
        SQL"""
        SELECT USERCODE FROM USER_NEWS_CATEGORY
        WHERE NEWS_CATEGORY_ID IN ($categoryIds) AND SELECTED = 0
        GROUP BY USERCODE HAVING COUNT(*) = ${categoryIds.size}
      """.as(str("usercode").*)
          .map(Usercode)
      ).toList
    }
  }

}
