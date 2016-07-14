package controllers.admin

import models.{Publisher, PublisherPermissionScope, PublishingAbility}
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.mvc.{ActionRefiner, Result, Results}
import services.dao.DepartmentInfoDao
import services.{NewsCategoryService, PublisherService, SecurityService}
import warwick.sso.AuthenticatedRequest

import scala.concurrent.Future

trait Publishing extends DepartmentOptions with CategoryOptions with ProviderOptions with PublishingActionRefiner {

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
      case PublisherPermissionScope.AllDepartments =>
        true
      case PublisherPermissionScope.Departments(deptCodes: Seq[String]) =>
        data.audience.forall(_.startsWith("Dept:")) &&
          data.department.forall(deptCodes.contains)
    }
  }

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

  implicit val executionContext = system.ThreadPools.web

  private val audienceDepartmentTypes = Set("ACADEMIC", "SERVICE")
  private val departmentInitialValue = Seq("" -> "--- Department ---")

  lazy val allPublishableDepartments =
    departmentInfoDao.allDepartments
      .filter(dept => audienceDepartmentTypes.contains(dept.`type`))
      .sortBy(_.name)

  def departmentOptions(implicit publisherRequest: PublisherRequest[_]) =
    departmentInitialValue ++ departmentsWithPublishPermission.map(dept => dept.code -> dept.name)

  def departmentsWithPublishPermission(implicit publisherRequest: PublisherRequest[_]) =
    permissionScope match {
      case PublisherPermissionScope.AllDepartments =>
        allPublishableDepartments
      case PublisherPermissionScope.Departments(deptCodes: Seq[String]) =>
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

  val publisherService: PublisherService

  val securityService: SecurityService

  import securityService._

  private def GetPublisher(id: String, requiredAbilities: Seq[PublishingAbility]) = new ActionRefiner[AuthenticatedRequest, PublisherRequest] {

    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, PublisherRequest[A]]] = {
      implicit val r = request
      val user = request.context.user.get

      Future.successful {
        publisherService.find(id).map { publisher =>
          val userRoles = publisherService.getRolesForUser(id, user.usercode)
          val can = requiredAbilities.forall(ability => userRoles.exists(_.can(ability)))

          if (can) {
            Right(new PublisherRequest(publisher, request))
          } else {
            Left(Results.Forbidden(views.html.errors.forbidden(user.name.first)))
          }
        }.getOrElse {
          Left(Results.NotFound(views.html.errors.notFound()))
        }
      }
    }

  }

  def PublisherAction(id: String, requiredAbilities: PublishingAbility*) = RequiredUserAction andThen GetPublisher(id, requiredAbilities)

}

class PublisherRequest[A](val publisher: Publisher, request: AuthenticatedRequest[A])
  extends AuthenticatedRequest[A](request.context, request.request)

