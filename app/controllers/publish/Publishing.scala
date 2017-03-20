package controllers.publish

import controllers.admin.addFormErrors
import models.Audience
import models.publishing._
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc.{ActionRefiner, AnyContent, Result, Results}
import services.dao.DepartmentInfoDao
import services.{NewsCategoryService, PublisherService, SecurityService}
import system.ImplicitRequestContext
import warwick.sso.AuthenticatedRequest

import scala.concurrent.Future

trait Publishing extends DepartmentOptions with CategoryOptions with ProviderOptions with PublishingActionRefiner {
  self: ImplicitRequestContext =>

  implicit val executionContext = system.ThreadPools.web

  val audienceBinder: AudienceBinder

  def audienceForm(implicit request: PublisherRequest[_]) = Form(
    single("audience" -> audienceMapping)
  )

  def audienceMapping(implicit publisherRequest: PublisherRequest[_]): Mapping[AudienceData] =
    mapping(
      "audience" -> seq(nonEmptyText),
      "department" -> optional(text)
    )(AudienceData.apply)(AudienceData.unapply)
      .verifying(
        "You do not have the required permissions to publish to that audience.",
        data => userCanPublishToAudience(data)
      )

  def permissionScope(implicit publisherRequest: PublisherRequest[_]) =
    publisherService.getPermissionScope(publisherRequest.publisher.id)

  private def userCanPublishToAudience(data: AudienceData)(implicit publisherRequest: PublisherRequest[_]) = {
    permissionScope match {
      case PermissionScope.AllDepartments =>
        true
      case PermissionScope.Departments(deptCodes: Seq[String]) =>
        data.audience.forall(_.startsWith("Dept:")) &&
          data.department.forall(deptCodes.contains)
    }
  }

  def bindFormWithAudience[A <: PublishableWithAudience](
    baseForm: Form[A],
    submitted: Boolean,
    renderForm: (Form[A]) => Result,
    onSubmit: ((A, Audience) => Result)
  )(implicit request: PublisherRequest[AnyContent]): Future[Result] = {
    val form = baseForm.bindFromRequest

    form.fold(
      formWithErrors => {
        // If the PublishNewsItemData form fails to bind, we can't display
        // validation errors for the audience.  Work around this by separately
        // binding the audience field and pushing any errors onto the top-level
        // form.
        val boundForm = audienceForm.bindFromRequest.fold(
          _ => Future.successful(formWithErrors),
          audienceData => audienceBinder.bindAudience(audienceData).map {
            case Left(errors) => addFormErrors(formWithErrors, errors)
            case Right(_) => formWithErrors
          }
        )

        boundForm.map(renderForm)
      },
      publish => {
        audienceBinder.bindAudience(publish.audience).map {
          case Left(errors) =>
            renderForm(addFormErrors(form, errors))
          case Right(audience) =>
            if (submitted) {
              onSubmit(publish, audience)
            } else {
              // If the form has not been submitted, just render the form again
              // having performed validation.
              renderForm(form)
            }
        }
      }
    )
  }


}

trait PublishableWithAudience {
  val audience: AudienceData
}

trait ProviderOptions {
  val publisherService: PublisherService

  def providerOptions(implicit publisherRequest: PublisherRequest[_]) =
    publisherService.getProviders(publisherRequest.publisher.id)
      .map(provider => provider.id -> provider.name)

}

trait DepartmentOptions {
  self: Publishing =>

  val departmentInfoDao: DepartmentInfoDao

  val publisherService: PublisherService

  private val audienceDepartmentTypes = Set("ACADEMIC", "SERVICE")

  lazy val allPublishableDepartments =
    departmentInfoDao.allDepartments
      .filter(dept => audienceDepartmentTypes.contains(dept.`type`))
      .sortBy(_.name)

  def departmentOptions(implicit publisherRequest: PublisherRequest[_]) =
    departmentsWithPublishPermission.map(dept => dept.code -> dept.name)

  def departmentsWithPublishPermission(implicit publisherRequest: PublisherRequest[_]) =
    permissionScope match {
      case PermissionScope.AllDepartments =>
        allPublishableDepartments
      case PermissionScope.Departments(deptCodes: Seq[String]) =>
        allPublishableDepartments.filter(dept => deptCodes.contains(dept.code))
    }

}

trait CategoryOptions {

  val newsCategoryService: NewsCategoryService

  // Rationale: after removing all possible options, anything that remains is invalid
  val categoryMappingAllowingEmpty = seq(nonEmptyText)
    .verifying("Some selections were invalid", _.diff(categoryOptions.map(_.id)).isEmpty)

  val categoryMapping = categoryMappingAllowingEmpty
    .verifying("You must select at least one category", _.nonEmpty)

  lazy val categoryOptions = newsCategoryService.all()

}

trait PublishingActionRefiner {
  self: ImplicitRequestContext =>

  val publisherService: PublisherService

  val securityService: SecurityService

  import securityService._

  private def GetPublisher(id: String, requiredAbilities: Seq[Ability]) = new ActionRefiner[AuthenticatedRequest, PublisherRequest] {

    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, PublisherRequest[A]]] = {
      implicit val r = request
      val user = request.context.user.get

      Future.successful {
        publisherService.find(id).map { publisher =>
          val userRole = publisherService.getRoleForUser(id, user.usercode)
          if (userRole.can(requiredAbilities)) {
            Right(new PublisherRequest(publisher, userRole, request))
          } else {
            Left(Results.Forbidden(views.html.errors.forbidden(user.name.first)))
          }
        }.getOrElse {
          Left(Results.NotFound(views.html.errors.notFound()))
        }
      }
    }

  }

  def PublisherAction(id: String, requiredAbilities: Ability*) = RequiredUserAction andThen GetPublisher(id, requiredAbilities)

}

class PublisherRequest[A](val publisher: Publisher, val userRole: Role, request: AuthenticatedRequest[A])
  extends AuthenticatedRequest[A](request.context, request.request)