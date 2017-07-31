package controllers.admin.publishers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.publishing.Publisher
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.{PublisherService, SecurityService}
import system.Roles

@Singleton
class PublishersController @Inject() (
  security: SecurityService,
  val messagesApi: MessagesApi,
  publisherService: PublisherService
) extends BaseController with I18nSupport {

  import Roles._
  import security._

  def allPublishers: Seq[Publisher] = publisherService.all

  def publisherForm = Form(mapping(
    "id" -> nonEmptyText.verifying("ID already exists", id => !allPublishers.exists(_.id == id)),
    "name" -> nonEmptyText,
    "maxRecipients" -> optional(number)
  )(Publisher.apply)(Publisher.unapply))

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val publishers = allPublishers
    val providerMap = publishers.map(publisher => publisher -> publisherService.getProviders(publisher.id)).toMap
    val userPermissionsMap = publishers.map(publisher => publisher -> publisherService.getPublisherPermissions(publisher.id)).toMap
    val permissionScopeMap = publishers.map(publisher => publisher -> publisherService.getPermissionScope(publisher.id)).toMap
    Ok(views.html.admin.publishers.index(publishers, providerMap, userPermissionsMap, permissionScopeMap))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.publishers.createForm(publisherForm))
  }

  def create = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    publisherForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.admin.publishers.createForm(formWithErrors)),
      data => {
        publisherService.save(data)
        auditLog('CreatePublisher, 'id -> data.id)
        Redirect(routes.PublishersController.index()).flashing("success" -> "Publisher created")
      }
    )
  }

}
