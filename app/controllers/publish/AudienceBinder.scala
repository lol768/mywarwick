package controllers.publish

import javax.inject.{Inject, Singleton}

import models.Audience
import models.Audience._
import play.api.data.FormError
import services.dao.DepartmentInfoDao
import uk.ac.warwick.util.core.StringUtils

import scala.concurrent.Future

/**
  * For converting bits of a raw request Form into an actual Audience.
  */
@Singleton
class AudienceBinder @Inject() (departments: DepartmentInfoDao) {

  /**
    * Attempts to convert the request parameters into an Audience object.
    * If there are any problems it returns a Seq of FormErrors.
    *
    * Return type is a future because it depends on the list of departments.
    */
  def bindAudience(data: AudienceData): Future[Either[Seq[FormError], Audience]] = {
    var errors = Seq.empty[FormError]

    if (data.audience.contains("Public")) {
      Future.successful(Right(Audience.Public))
    } else {

      val groupedComponents = data.audience.groupBy(_.startsWith("Dept:"))

      // Bits of audience not related to a department.
      val globalComponents = groupedComponents.getOrElse(false, Nil).flatMap {
        case Audience.ComponentParameter(component) => Some(component)
        case unrecognised =>
          errors :+= FormError("audience", "error.audience.invalid", unrecognised)
          None
      }

      val deptComponentValues = groupedComponents.getOrElse(true, Nil)
        .map(_.replaceFirst("^Dept:", ""))
        .flatMap {
          case Audience.DepartmentSubset(subset) => Some(subset)
          case unrecognised =>
            errors :+= FormError("audience", "error.audience.invalid", s"Dept:$unrecognised")
            None
        }

      val departmentParam = data.department.filter(StringUtils.hasText).map(_.trim)
      val department = departmentParam.flatMap { code =>
        departments.allDepartments.find(_.code == code)
      }
      val deptComponent = department match {
        case Some(d) if deptComponentValues.nonEmpty => Some(DepartmentAudience(d.code, deptComponentValues))
        case Some(d) =>
          errors :+= FormError("audience", "error.audience.noDepartmentSubsets")
          None
        case None if data.department.isDefined =>
          errors :+= FormError("department", "error.department.invalid")
          None
        case None => None // No department audience - no problem
      }

      if (department.isEmpty && globalComponents.isEmpty) {
        errors :+= FormError("audience", "error.audience.empty")
      }

      Future.successful {
        if (errors.nonEmpty) {
          Left(errors)
        } else {
          Right(Audience(globalComponents ++ deptComponent))
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
        case component: Component => Seq(component.entryName)
        case _ => Seq.empty
      }
    }

    AudienceData(components, department)
  }

}