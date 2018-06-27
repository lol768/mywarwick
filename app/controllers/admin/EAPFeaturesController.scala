package controllers.admin

import com.google.inject.Inject
import controllers.MyController
import javax.inject.Singleton
import models.{DateFormats, EAPFeatureSave}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent}
import services.{EAPFeaturesService, SecurityService}
import system.Roles

@Singleton
class EAPFeaturesController @Inject() (
  security: SecurityService,
  eapFeaturesService: EAPFeaturesService
) extends MyController with I18nSupport {

  private val form = Form(mapping(
    "name" -> nonEmptyText,
    "startDate" -> optional(DateFormats.dateLocalMapping),
    "endDate" -> optional(DateFormats.dateLocalMapping),
    "summary" -> optional(text),
    "feedbackUrl" -> optional(text)
  )(EAPFeatureSave.apply)(EAPFeatureSave.unapply))

  val confirmForm = Form(mapping(
    "confirm" -> checked("You must check this box")
  )(b => b)(b => Some(b)))

  import Roles._
  import security._

  def index: Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.eap.index(eapFeaturesService.all))
  }

  def createForm: Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.eap.createForm(form))
  }

  def create: Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => Ok(views.html.admin.eap.createForm(formWithErrors)),
      data => {
        val id = eapFeaturesService.save(data)
        auditLog('CreateEAPFeature, 'id -> id)
        Redirect(routes.EAPFeaturesController.index()).flashing("success" -> "Feature created")
      }
    )
  }

  def updateForm(id: String): Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    eapFeaturesService.getById(id).map { feature =>
      Ok(views.html.admin.eap.updateForm(form.fill(feature.toSave), id))
    }.getOrElse(NotFound(views.html.errors.notFound()))
  }

  def update(id: String): Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    eapFeaturesService.getById(id).map { _ =>
      form.bindFromRequest.fold(
        formWithErrors => Ok(views.html.admin.eap.updateForm(formWithErrors, id)),
        data => {
          eapFeaturesService.update(id, data)
          auditLog('UpdateEAPFeature, 'id -> id)
          Redirect(routes.EAPFeaturesController.index()).flashing("success" -> "Feature updated")
        }
      )
    }.getOrElse(NotFound(views.html.errors.notFound()))
  }

  def deleteForm(id: String): Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    eapFeaturesService.getById(id).map { _ =>
      Ok(views.html.admin.eap.deleteForm(confirmForm, id))
    }.getOrElse(NotFound(views.html.errors.notFound()))
  }

  def delete(id: String): Action[AnyContent] = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    eapFeaturesService.getById(id).map { _ =>
      confirmForm.bindFromRequest.fold(
        formWithErrors => Ok(views.html.admin.eap.deleteForm(formWithErrors, id)),
        _ => {
          eapFeaturesService.delete(id)
          auditLog('DeleteEAPFeature, 'id -> id)
          Redirect(routes.EAPFeaturesController.index()).flashing("success" -> "Feature deleted")
        }
      )
    }.getOrElse(NotFound(views.html.errors.notFound()))
  }

}
