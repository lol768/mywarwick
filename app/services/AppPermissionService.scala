package services

import com.google.inject.{ImplementedBy, Inject}
import services.dao.ProviderPermissionDao
import warwick.sso.User

@ImplementedBy(classOf[ProviderPermissionServiceImpl])
trait ProviderPermissionService {

  def canUserPostForProvider(providerId: String, user: User): Boolean

}

class ProviderPermissionServiceImpl @Inject()(
  providerPermissionDao: ProviderPermissionDao
) extends ProviderPermissionService {

  override def canUserPostForProvider(providerId: String, user: User): Boolean =
    providerPermissionDao.canUserPostForApp(providerId, user.usercode.string)

}