package controllers.admin.publishers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import models.publishing.{PermissionScope, Publisher, PublisherSave}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.{DepartmentInfoService, PublisherService, SecurityService}
import system.Roles

@Singleton
class PublisherDepartmentsController @Inject() (
  security: SecurityService,
  val publisherService: PublisherService,
  departmentInfoService: DepartmentInfoService
) extends MyController with I18nSupport with WithPublisher {

  import Roles._
  import security._

  case class DepartmentsData(isAllDepartments: Boolean, departments: Seq[String])

  def departmentDataForm = Form(mapping(
    "isAllDepartments" -> boolean,
    "departments" -> seq(nonEmptyText)
  )(DepartmentsData.apply)(DepartmentsData.unapply))

  def form(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { publisher =>
      val permissionScope = publisherService.getPermissionScope(publisherId)
      val (isAllDepartments, currentDepartmentCodes) = permissionScope match {
        case PermissionScope.AllDepartments => (true, Nil)
        case PermissionScope.Departments(depts) => (false, depts)
      }
      Ok(views.html.admin.publishers.departmentsForm(publisher, isAllDepartments, currentDepartmentCodes, departmentInfoService.allPublishableDepartments))
    })
  }

  def update(publisherId: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    withPublisher(publisherId, { _ =>
      departmentDataForm.bindFromRequest.fold(
        formWithErrors => BadRequest(s"${formWithErrors.errors.map(_.message).mkString(", ")}"),
        data => {
          publisherService.updatePermissionScope(publisherId, data.isAllDepartments, data.departments.distinct)
          auditLog('UpdatePublisherDepartments, 'id -> publisherId)
          Redirect(routes.PublishersController.index()).flashing("success" -> "Publisher departments updated")
        }
      )
    })
  }

}
