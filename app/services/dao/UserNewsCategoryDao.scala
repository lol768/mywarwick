package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import warwick.sso.Usercode

@ImplementedBy(classOf[UserNewsCategoryDaoImpl])
trait UserNewsCategoryDao {

  def getSubscribedCategories(usercode: Usercode)(implicit c: Connection): Seq[String]

  def setSubscribedCategories(usercode: Usercode, categoryIds: Seq[String])(implicit c: Connection): Unit

}

@Singleton
class UserNewsCategoryDaoImpl extends UserNewsCategoryDao {

  override def getSubscribedCategories(usercode: Usercode)(implicit c: Connection) =
    SQL"SELECT NEWS_CATEGORY_ID FROM USER_NEWS_CATEGORY WHERE USERCODE = ${usercode.string}"
      .executeQuery()
      .as(scalar[String].*)

  override def setSubscribedCategories(usercode: Usercode, categoryIds: Seq[String])(implicit c: Connection) = {
    SQL"DELETE FROM USER_NEWS_CATEGORY WHERE USERCODE = ${usercode.string}"
      .execute()

    categoryIds.foreach { categoryId =>
      SQL"INSERT INTO USER_NEWS_CATEGORY (USERCODE, NEWS_CATEGORY_ID) VALUES (${usercode.string}, $categoryId)"
        .execute()
    }
  }

}
