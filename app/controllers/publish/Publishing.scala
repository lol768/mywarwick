package controllers.publish

import controllers.admin.addFormErrors
import models.publishing._
import models.{API, Audience, NewsCategory}
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import services._
import services.dao.DepartmentInfo
import system.{ImplicitRequestContext, Logging, ThreadPools}
import warwick.sso.{AuthenticatedRequest, Usercode}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait Publishing extends DepartmentOptions with CategoryOptions with ProviderOptions with PublishingActionRefiner with Logging {
  self: ImplicitRequestContext with BaseController =>

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
        data.department.forall(deptCodes.contains)
    }
  }

  def bindFormWithAudience[A <: PublishableWithAudience](
    baseForm: Form[A],
    submitted: Boolean,
    restrictedRecipients: Boolean,
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
          audienceData => audienceBinder.bindAudience(audienceData, restrictedRecipients).map {
            case Left(errors) => addFormErrors(formWithErrors, errors)
            case Right(_) => formWithErrors
          }
        )

        boundForm.map(renderForm)
      },
      publish => {
        audienceBinder.bindAudience(publish.audience, restrictedRecipients).map {
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

  case class GroupedUsercodes(baseAudience: Set[Usercode], groupedUsercodes: Map[String, Set[Usercode]])

  def sharedAudienceInfo(
    audienceService: AudienceService,
    processGroupedUsercodes: Map[Audience.Component, Set[Usercode]] => GroupedUsercodes,
    newsCategories: Option[Set[NewsCategory]] = Option.empty
  )(implicit request: PublisherRequest[_]): Future[Result] =
    audienceForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", formWithErrors.errors.map(e => API.Error(e.key, e.message)))))),
      audienceData => {
        audienceBinder.bindAudience(audienceData).map {
          case Left(errors) => BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", errors.map(e => API.Error(e.key, e.message)))))
          case Right(audience) =>
            if (audience.public) {
              Ok(Json.toJson(API.Success[JsObject](data = Json.obj(
                "public" -> true
              ))))
            } else {
              audienceService.resolveUsersForComponentsGrouped(audience.components).map(_.toMap).map(processGroupedUsercodes) match {
                case Success(groupedUsercodes) =>
                  // then intersect with news category
                  // TODO

                  val jsonData = if (newsCategories.isEmpty) {
                    Json.obj(
                      "baseAudience" -> groupedUsercodes.baseAudience.size,
                      "groupedAudience" -> groupedUsercodes.groupedUsercodes.map {
                        case (groupName, usercodes) => (groupName, usercodes.size)
                      }
                    )
                  } else {
                    Json.obj(
                      "baseAudience" -> groupedUsercodes.baseAudience.size,
                      "groupedAudience" -> groupedUsercodes.groupedUsercodes.map {
                        case (groupName, usercodes) => (groupName, usercodes.size)
                      }
                    )
                  }
                  Ok(Json.toJson(API.Success[JsObject](data = jsonData)))
                case Failure(err) =>
                  logger.error("Failed to resolve audience", err)
                  InternalServerError(Json.toJson(API.Failure[JsObject]("Internal Server Error", Seq(API.Error("resolve-audience", "Failed to resolve audience")))))
              }
            }
        }
      }
    )
}

trait PublishableWithAudience {
  val audience: AudienceData
}

trait ProviderOptions {
  val publisherService: PublisherService

  def providerOptions(implicit publisherRequest: PublisherRequest[_]): Seq[(String, String)] =
    publisherService.getProviders(publisherRequest.publisher.id)
      .map(provider => provider.id -> provider.name.getOrElse(provider.id))

}

trait DepartmentOptions {
  self: Publishing =>

  val departmentInfoService: DepartmentInfoService

  val publisherService: PublisherService

  lazy val allDepartments: Seq[DepartmentInfo] = departmentInfoService.allDepartments

  lazy val allPublishableDepartments = departmentInfoService.allPublishableDepartments

  def departmentOptions(implicit publisherRequest: PublisherRequest[_]): Seq[DepartmentInfo] =
    departmentsWithPublishPermission

  def departmentsWithPublishPermission(implicit publisherRequest: PublisherRequest[_]): Seq[DepartmentInfo] =
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

    override protected def executionContext = ThreadPools.web

  }

  def PublisherAction(id: String, requiredAbilities: Ability*) = RequiredUserAction andThen GetPublisher(id, requiredAbilities)

}

class PublisherRequest[A](val publisher: Publisher, val userRole: Role, request: AuthenticatedRequest[A])
  extends AuthenticatedRequest[A](request.context, request.request)
