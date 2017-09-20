package controllers.admin.publishers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.publishing.Publisher
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Result
import services.{ProviderRender, ProviderSave, PublisherService, SecurityService}
import system.{RequestContext, Roles}

@Singleton
class ProvidersController @Inject() (
  security: SecurityService,
  val messagesApi: MessagesApi,
  val publisherService: PublisherService
) extends BaseController with I18nSupport with WithPublisher {

  import Roles._
  import security._

  def allProviders(publisherId: String): Seq[ProviderRender] = publisherService.getProviders(publisherId)

  def createProviderIdForm(publisherId: String) = Form(mapping(
    "id" -> nonEmptyText
      .verifying(Constraints.pattern("[a-z-]+".r))
      .verifying("ID already exists", id => !allProviders(publisherId).exists(_.id == id))
  )(s => s)(s => Option(s)))

  def providerForm = Form(mapping(
    "name" -> optional(text),
    "icon" -> optional(text),
    "colour" -> optional(text),
    "sendEmail" -> boolean
  )(ProviderSave.apply)(ProviderSave.unapply))

  def createForm(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { publisher =>
      Ok(views.html.admin.providers.createForm(publisher, createProviderIdForm(publisherId), providerForm))
    })
  }

  def create(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { publisher =>
      createProviderIdForm(publisherId).bindFromRequest.fold(
        idFormWithErrors => Ok(views.html.admin.providers.createForm(publisher, idFormWithErrors, providerForm.bindFromRequest)),
        id => {
          providerForm.bindFromRequest.fold(
            formWithErrors => Ok(views.html.admin.providers.createForm(publisher, createProviderIdForm(publisherId).bindFromRequest, formWithErrors)),
            data => {
              publisherService.saveProvider(publisherId, id, data)
              auditLog('CreateProvider, 'id -> id, 'publisherId -> publisherId)
              Redirect(routes.PublishersController.index()).flashing("success" -> "Provider created")
            }
          )
        }
      )
    })
  }

  def updateForm(publisherId: String, providerId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisherAndProvider(publisherId, providerId, { (publisher, provider) =>
      Ok(views.html.admin.providers.updateForm(
        publisher,
        providerId,
        providerForm.fill(ProviderSave(provider.name, provider.icon, provider.colour, provider.sendEmail))))
    })
  }

  def update(publisherId: String, providerId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisherAndProvider(publisherId, providerId, { (publisher, provider) =>
      providerForm.bindFromRequest.fold(
        formWithErrors => Ok(views.html.admin.providers.updateForm(publisher, publisherId, formWithErrors)),
        data => {
          publisherService.updateProvider(publisherId, providerId, data)
          auditLog('UpdateProvider, 'id -> providerId, 'publisherId -> publisherId)
          Redirect(routes.PublishersController.index()).flashing("success" -> "Provider updated")
        }
      )
    })
  }

  private def withPublisherAndProvider(publisherId: String, providerId: String, block: (Publisher, ProviderRender) => Result)(implicit request: RequestContext): Result = {
    publisherService.find(publisherId)
      .flatMap(publisher =>
        publisherService.getProviders(publisherId).find(_.id == providerId).map(provider =>
          block(publisher, provider)
        )
      )
      .getOrElse(NotFound(views.html.errors.notFound()))
  }
}
