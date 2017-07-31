package controllers.admin.publishers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.publishing.{Publisher, PublisherSave}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Result
import services.{PublisherService, SecurityService}
import system.{RequestContext, Roles}

@Singleton
class PublishersController @Inject() (
  security: SecurityService,
  val messagesApi: MessagesApi,
  publisherService: PublisherService
) extends BaseController with I18nSupport {

  import Roles._
  import security._

  def allPublishers: Seq[Publisher] = publisherService.all

  def createPublisherIdForm = Form(mapping(
    "id" -> nonEmptyText.verifying("ID already exists", id => !allPublishers.exists(_.id == id))
  )(s => s)(s => Option(s)))

  def publisherForm = Form(mapping(
    "name" -> nonEmptyText,
    "maxRecipients" -> optional(number)
  )(PublisherSave.apply)(PublisherSave.unapply))

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val publishers = allPublishers
    val providerMap = publishers.map(publisher => publisher -> publisherService.getProviders(publisher.id)).toMap
    val userPermissionsMap = publishers.map(publisher => publisher -> publisherService.getPublisherPermissions(publisher.id)).toMap
    val permissionScopeMap = publishers.map(publisher => publisher -> publisherService.getPermissionScope(publisher.id)).toMap
    Ok(views.html.admin.publishers.index(publishers, providerMap, userPermissionsMap, permissionScopeMap))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.publishers.createForm(createPublisherIdForm, publisherForm))
  }

  def create = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    createPublisherIdForm.bindFromRequest.fold(
      idFormWithErrors => Ok(views.html.admin.publishers.createForm(idFormWithErrors, publisherForm.bindFromRequest)),
      id => {
        publisherForm.bindFromRequest.fold(
          formWithErrors => Ok(views.html.admin.publishers.createForm(createPublisherIdForm.bindFromRequest, formWithErrors)),
          data => {
            publisherService.save(id, data)
            auditLog('CreatePublisher, 'id -> id)
            Redirect(routes.PublishersController.index()).flashing("success" -> "Publisher created")
          }
        )
      }
    )

  }

  def updateForm(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { publisher =>
      Ok(views.html.admin.publishers.updateForm(publisherId, publisherForm.fill(PublisherSave(publisher.name, publisher.maxRecipients))))
    })
  }

  def update(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { _ =>
      publisherForm.bindFromRequest.fold(
        formWithErrors => Ok(views.html.admin.publishers.updateForm(publisherId, formWithErrors)),
        data => {
          publisherService.update(publisherId, data)
          auditLog('UpdatePublisher, 'id -> publisherId)
          Redirect(routes.PublishersController.index()).flashing("success" -> "Publisher updated")
        }
      )
    })
  }

  private def withPublisher(publisherId: String, block: (Publisher) => Result)(implicit request: RequestContext): Result = {
    publisherService.find(publisherId)
      .map(block)
      .getOrElse(NotFound(views.html.errors.notFound()))
  }

}
