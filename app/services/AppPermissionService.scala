package services

import com.google.inject.{ImplementedBy, Inject}
import warwick.sso.User

@ImplementedBy(classOf[AppPermissionServiceImpl])
trait AppPermissionService {

  def canUserPostForApp(appId: String, user: User): Boolean

}

class AppPermissionServiceImpl @Inject()(
  appPermissionDao: AppPermissionDao
) extends AppPermissionService {

  override def canUserPostForApp(appId: String, user: User): Boolean =
    appPermissionDao.canUserPostForApp(appId, user.usercode.string)

}