package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.db.{Database, NamedDatabase}
import services.dao.UserNewsCategoryDao
import warwick.sso.Usercode

@ImplementedBy(classOf[UserNewsCategoryServiceImpl])
trait UserNewsCategoryService {

  def getSubscribedCategories(usercode: Usercode): Seq[String]

  def setSubscribedCategories(usercode: Usercode, categoryIds: Seq[String]): Unit

  def getUsercodesSubscribedToAllCategories(categoryIds: Seq[String]): Seq[Usercode]

}

@Singleton
class UserNewsCategoryServiceImpl @Inject()(
  dao: UserNewsCategoryDao,
  @NamedDatabase("default") db: Database
) extends UserNewsCategoryService {

  override def getSubscribedCategories(usercode: Usercode) =
    db.withConnection(implicit c => dao.getSubscribedCategories(usercode))

  override def setSubscribedCategories(usercode: Usercode, categoryIds: Seq[String]) =
    db.withTransaction(implicit c => dao.setSubscribedCategories(usercode, categoryIds))

  override def getUsercodesSubscribedToAllCategories(categoryIds: Seq[String]) =
    db.withConnection(implicit c => dao.getUsercodesSubscribedToAllCategories(categoryIds))

}
