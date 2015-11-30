package services

import com.google.inject.{ImplementedBy, Inject}
import play.api.db.{Database, NamedDatabase}
import services.dao.ProviderPermissionDao
import warwick.sso.User

@ImplementedBy(classOf[ProviderPermissionServiceImpl])
trait ProviderPermissionService {

  def canUserPostForProvider(providerId: String, user: User): Boolean

}

class ProviderPermissionServiceImpl @Inject()(
  providerPermissionDao: ProviderPermissionDao,
  @NamedDatabase("default") db: Database
) extends ProviderPermissionService {

  override def canUserPostForProvider(providerId: String, user: User): Boolean =
    db.withConnection(implicit c => providerPermissionDao.canUserPostForProvider(providerId, user.usercode.string))

}