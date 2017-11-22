package controllers.admin.publishers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.publishing.{PublisherPermission, PublishingRole}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.{PublisherService, SecurityService}
import system.Roles
import warwick.sso.{UserLookupService, Usercode}

import scala.util.Try

case class PublisherPermissionData(
  usercode: String,
  role: String
)

@Singleton
class PublisherPermissionsController @Inject() (
  security: SecurityService,
  val publisherService: PublisherService,
  userLookupService: UserLookupService
) extends BaseController with I18nSupport with WithPublisher {

  import Roles._
  import security._

  def permissionsDataForm = Form(mapping(
    "permissions" -> seq(mapping(
      "usercode" -> nonEmptyText,
      "role" -> nonEmptyText.verifying("Unknown role", r => Try(PublishingRole.withName(r)).isSuccess)
    )(PublisherPermissionData.apply)(PublisherPermissionData.unapply))
  )(l => l)(l => Option(l)))

  def form(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { publisher =>
      val permissions = publisherService.getPublisherPermissions(publisherId).sortBy(_.usercode.string)
      Ok(views.html.admin.publishers.permissionsForm(
        publisher,
        permissionsDataForm.fill(permissions.map(p => PublisherPermissionData(p.usercode.string, p.role.toString))),
        userLookupService.getUsers(permissions.map(_.usercode)).toOption.getOrElse(Map.empty)
      ))
    })
  }

  def update(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { _ =>
      permissionsDataForm.bindFromRequest.fold(
        formWithErrors => BadRequest(s"${formWithErrors.errors.map(_.message).mkString(", ")}"),
        data => {
          publisherService.updatePublisherPermissions(publisherId, data.distinct.map(p => PublisherPermission(
            Usercode(p.usercode),
            PublishingRole.withName(p.role)
          )))
          auditLog('UpdatePublisherPermissions, 'id -> publisherId)
          Redirect(routes.PublishersController.index()).flashing("success" -> "Publisher permissions updated")
        }
      )
    })
  }

}
