package services
import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.{EAPFeatureRender, EAPFeatureSave}
import play.api.db.{Database, NamedDatabase}
import services.dao.EAPFeaturesDao

@ImplementedBy(classOf[EAPFeaturesServiceImpl])
trait EAPFeaturesService {

  def all: Seq[EAPFeatureRender]

  def save(feature: EAPFeatureSave): String

  def update(id: String, feature: EAPFeatureSave): Unit

  def delete(id: String): Unit

  def getById(id: String): Option[EAPFeatureRender]

}

class EAPFeaturesServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: EAPFeaturesDao,
) extends EAPFeaturesService {

  override def all: Seq[EAPFeatureRender] = db.withConnection { implicit c =>
    dao.all
  }

  override def save(feature: EAPFeatureSave): String = db.withConnection { implicit c =>
    dao.save(feature)
  }

  override def update(id: String, feature: EAPFeatureSave): Unit = db.withConnection { implicit c =>
    dao.update(id, feature)
  }

  override def delete(id: String): Unit = db.withConnection { implicit c =>
    dao.delete(id)
  }

  override def getById(id: String): Option[EAPFeatureRender] = db.withConnection { implicit c =>
    dao.getById(id)
  }

}