package services.dao

import anorm.SqlParser._
import com.google.inject.{ImplementedBy, Inject}
import play.api.db.Database
import play.db.NamedDatabase

@ImplementedBy(classOf[ProviderPermissionDaoImpl])
trait ProviderPermissionDao {

  def canUserPostForApp(providerId: String, usercode: String): Boolean

}

class ProviderPermissionDaoImpl @Inject()(@NamedDatabase("default") db: Database) extends ProviderPermissionDao {

  private val NO_PERMISSION = 0

  override def canUserPostForApp(providerId: String, usercode: String): Boolean =
    db.withConnection { implicit c =>
      anorm.SQL("SELECT COUNT(*) FROM PROVIDER_PERMISSION WHERE PROVIDER_ID = {providerId} AND USERCODE = {usercode}")
        .on(
          'providerId -> providerId,
          'usercode -> usercode
        )
        .as(scalar[Int].single) > NO_PERMISSION
    }

}
