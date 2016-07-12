package controllers.admin

import models.{Publisher, PublishingAbility}
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.mvc.{ActionRefiner, Result, Results, WrappedRequest}
import services.{NewsCategoryService, PublisherService, SecurityService}
import services.dao.{DepartmentInfo, DepartmentInfoDao}
import warwick.sso.AuthenticatedRequest

import scala.concurrent.Future

trait Publishing extends DepartmentOptions with CategoryOptions with PublishingActionRefiner {

  val audienceMapping: Mapping[AudienceData] = mapping(
    "audience" -> seq(nonEmptyText),
    "department" -> optional(text)
  )(AudienceData.apply)(AudienceData.unapply)

}

trait DepartmentOptions {

  val departmentInfoDao: DepartmentInfoDao

  implicit val executionContext = system.ThreadPools.web

  private val departmentTypes = Set("ACADEMIC", "SERVICE")
  private val departmentInitialValue = Seq("" -> "--- Department ---")

  def departmentOptions =
    toDepartmentOptions(departmentInfoDao.allDepartments)

  def toDepartmentOptions(depts: Seq[DepartmentInfo]) =
    departmentInitialValue ++ depts.filter { info => departmentTypes.contains(info.`type`) }
      .sortBy { info => info.name }
      .map { info => info.code -> info.name }
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

  class PublisherRequest[A](val publisher: Publisher, request: AuthenticatedRequest[A])
    extends WrappedRequest[A](request) {
    val context = request.context
  }

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
