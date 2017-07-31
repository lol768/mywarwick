package controllers.admin.publishers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import services.{PublisherService, SecurityService}
import system.Roles

@Singleton
class PublishersController @Inject() (
  security: SecurityService,
  publisherService: PublisherService
) extends BaseController {

  import Roles._
  import security._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val publishers = publisherService.all
    val providerMap = publishers.map(publisher => publisher -> publisherService.getProviders(publisher.id)).toMap
    val userPermissionsMap = publishers.map(publisher => publisher -> publisherService.getPublisherPermissions(publisher.id)).toMap
    val permissionScopeMap = publishers.map(publisher => publisher -> publisherService.getPermissionScope(publisher.id)).toMap
    Ok(views.html.admin.publishers.index(publishers, providerMap, userPermissionsMap, permissionScopeMap))
  }

}
