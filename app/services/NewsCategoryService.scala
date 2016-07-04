package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.NewsCategory
import play.api.db.Database
import play.db.NamedDatabase
import services.dao.NewsCategoryDao


@ImplementedBy(classOf[NewsCategoryServiceImpl])
trait NewsCategoryService {

  def all(): Seq[NewsCategory]

}

@Singleton
class NewsCategoryServiceImpl @Inject()(
  dao: NewsCategoryDao,
  @NamedDatabase("default") db: Database
) extends NewsCategoryService {

  override def all() = db.withConnection(implicit c => dao.all())

}

