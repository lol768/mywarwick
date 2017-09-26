package controllers.publish

import javax.inject.{Inject, Singleton}

import models.Audience
import models.Audience._
import models.publishing.PermissionScope
import models.publishing.PermissionScope.{AllDepartments, Departments}
import play.api.data.FormError
import services.{AudienceService, PublisherService}
import services.dao.DepartmentInfoDao
import uk.ac.warwick.util.core.StringUtils
import warwick.sso.GroupName

import scala.concurrent.Future

/**
  * For converting bits of a raw request Form into an actual Audience.
  */
@Singleton
class AudienceBinder @Inject()(
  departments: DepartmentInfoDao,
  audienceService: AudienceService,
  publisherService: PublisherService
) {

  /**
    * Attempts to convert the request parameters into an Audience object.
    * If there are any problems it returns a Seq of FormErrors.
    *
    * Return type is a future because it depends on the list of departments.
    */
  def bindAudience(data: AudienceData, restrictedRecipients: Boolean = false)(implicit publisherRequest: PublisherRequest[_]): Future[Either[Seq[FormError], Audience]] = {
    var errors = Seq.empty[FormError]
    val scope: PermissionScope = publisherService.getPermissionScope(publisherRequest.publisher.id)

    if (data.audience.contains("Public")) {
      if (restrictedRecipients) {
        Future.successful(Left(Seq(FormError("audience", "error.audience.tooMany.public"))))
      } else {
        Future.successful(Right(Audience.Public))
      }
    } else {

      val groupedComponents = data.audience.groupBy(_.startsWith("Dept:"))

      // Bits of audience not related to a department.
      val nonDeptComponents = groupedComponents.getOrElse(false, Nil).flatMap {
        case Audience.ComponentParameter(component) => Some(component)

        // Don't error for a blank audience such as "WebGroup:"
        case unrecognised if unrecognised.endsWith(":") => None
        case unrecognised =>
          errors :+= FormError("audience", "error.audience.invalid", Seq(unrecognised))
          None
      }

      def validateUsercodesAudience(component: UsercodesAudience): Option[UsercodesAudience] = {
        val invalidUsercodes = component.usercodes.diff(audienceService.validateUsercodes(component.usercodes))
        if (invalidUsercodes.isEmpty)
          Some(UsercodesAudience(component.usercodes))
        else {
          errors :+= FormError("audience", "error.audience.usercodes.invalid", Seq(invalidUsercodes.map(_.string).mkString(", ")))
          None
        }
      }

      val globalComponents = nonDeptComponents.flatMap {
        case ua: UsercodesAudience => validateUsercodesAudience(ua)
        case component: Audience.Component => Some(component)
      }

      val deptComponentValues = groupedComponents.getOrElse(true, Nil)
        .map(_.replaceFirst("^Dept:", ""))
        .flatMap {
          case Audience.DepartmentSubset(subset) => Some(subset)
          case unrecognised =>
            errors :+= FormError("audience", "error.audience.invalid", Seq(s"Dept:$unrecognised"))
            None
        }
        .flatMap {
          case ua: UsercodesAudience => validateUsercodesAudience(ua)
          case ds: DepartmentSubset => Some(ds)
        }

      val departmentParam = data.department.filter(StringUtils.hasText).map(_.trim)
      lazy val allDepartments = departments.allDepartments
      val department = departmentParam.flatMap { code =>
        allDepartments.find(_.code == code)
      }

      val deptComponent = department match {
        case Some(d) if deptComponentValues.nonEmpty => Some(DepartmentAudience(d.code, deptComponentValues))
        case Some(d) if globalComponents.isEmpty =>
          errors :+= FormError("audience", "error.audience.noDepartmentSubsets")
          None
        case None if data.department.isDefined =>
          errors :+= FormError("department", "error.department.invalid")
          None
        case _ => None // No department audience - no problem
      }

      if (department.isEmpty && globalComponents.isEmpty) {
        errors :+= FormError("audience", "error.audience.empty")
      }

      val permissibleComponents: Seq[Component] = {
        val allComponents = globalComponents ++ deptComponent.toSeq
        scope match {
          case AllDepartments => allComponents // ok, is God
          case Departments(deptCodes) => allComponents.flatMap {
            case ds: DepartmentSubset => {
              errors :+= FormError("audience", "error.audience.noPermission", Seq(ds.displayName))
              None
            }
            case da: DepartmentAudience if deptCodes.contains(da.deptCode) => Some(da)
            case da: DepartmentAudience => {
              errors :+= FormError("department", "error.department.noPermission", Seq(allDepartments.find(_.code == da.deptCode).map(_.name).getOrElse("")))
              None
            }
            case component => Some(component)
          }
        }
      }

      if (errors.isEmpty && restrictedRecipients) {
        publisherRequest.publisher.maxRecipients.foreach { maxRecipients =>
          val recipients = audienceService.resolve(Audience(permissibleComponents)).toOption.map(_.size).getOrElse(0)
          if (recipients > maxRecipients) {
            errors :+= FormError("audience", "error.audience.tooMany", Seq(maxRecipients))
          }
        }
      }

      Future.successful {
        if (errors.nonEmpty) {
          Left(errors)
        } else {
          Right(Audience(permissibleComponents))
        }
      }
    }
  }

  def unbindAudience(audience: Audience): AudienceData = {
    val department = audience.components.map {
      case DepartmentAudience(deptCode, _) => Some(deptCode)
      case _ => None
    }.find(_.isDefined).flatten

    val components = if (audience.public) {
      Seq("Public")
    } else {
      audience.components.flatMap {
        case DepartmentAudience(_, subsets) => subsets.map(_.entryName).map("Dept:".concat)
        case WebGroupAudience(GroupName(groupName)) => Seq(s"WebGroup:$groupName")
        case ModuleAudience(moduleCode) => Seq(s"Module:$moduleCode")
        case SeminarGroupAudience(groupId) => Seq(s"SeminarGroup:$groupId")
        case RelationshipAudience(relationshipType, agentId) => Seq(s"Relationship:$relationshipType:${agentId.string}")
        case UsercodesAudience(usercodes) => usercodes.map(_.string)
        case component: Component => Seq(component.entryName)
        case _ => Seq.empty
      }
    }

    AudienceData(components, department)
  }

}
