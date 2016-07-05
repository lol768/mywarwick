package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.NewsCategory
import play.api.db.Database
import play.db.NamedDatabase
import services.dao.NewsCategoryDao


@ImplementedBy(classOf[NewsCategoryServiceImpl])
trait NewsCategoryService {

  def all(): Seq[NewsCategory]

  def saveNewsCategories(newsItemId: String, categoryIds: Seq[String]): Unit

  def getNewsCategories(newsItemId: String): Seq[NewsCategory]

  def updateNewsCategories(newsItemId: String, categoryIds: Seq[String]): Unit
}

@Singleton
class NewsCategoryServiceImpl @Inject()(
  dao: NewsCategoryDao,
  @NamedDatabase("default") db: Database
) extends NewsCategoryService {

  override def all() = db.withConnection(implicit c => dao.all())

  override def saveNewsCategories(newsItemId: String, categoryIds: Seq[String]) =
    db.withConnection(implicit c => dao.saveNewsCategories(newsItemId, categoryIds))

  override def updateNewsCategories(newsItemId: String, categoryIds: Seq[String]) =
    db.withConnection { implicit c =>
      dao.deleteNewsCategories(newsItemId)
      dao.saveNewsCategories(newsItemId, categoryIds)
    }

  override def getNewsCategories(newsItemId: String) =
    db.withConnection(implicit c => dao.getNewsCategories(newsItemId))

}

