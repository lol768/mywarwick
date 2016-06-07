package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.PublishCategory
import play.api.db.Database
import play.db.NamedDatabase
import services.dao.PublishCategoryDao
import warwick.anorm.converters.ColumnConversions._


@ImplementedBy(classOf[PublishCategoryServiceImpl])
trait PublishCategoryService {

  def all(): Seq[PublishCategory]

}

@Singleton
class PublishCategoryServiceImpl @Inject()(
  publishCategoryDao: PublishCategoryDao,
  @NamedDatabase("default") db: Database
) extends PublishCategoryService {

  override def all() = db.withConnection(implicit c => publishCategoryDao.all())

}

