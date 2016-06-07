package controllers.admin

import javax.inject.{Inject, Singleton}

import models.news.Audience
import models.news.Audience.DepartmentAudience
import play.api.data.FormError
import services.dao.DepartmentInfoDao
import uk.ac.warwick.util.core.StringUtils

import scala.concurrent.Future

/**
  * For converting bits of a raw request Form into an actual Audience.
  */
@Singleton
class AudienceBinder @Inject() (departments: DepartmentInfoDao) {
  import system.ThreadPools.web

  /**
    * Attempts to convert the request parameters into an Audience object.
    * If there are any problems it returns a Seq of FormErrors.
    *
    * Return type is a future because it depends on the list of departments.
    */
  def bindAudience(data: AudienceFormData): Future[Either[Seq[FormError], Audience]] = for {
    allDepts <- departments.allDepartments
  } yield {
    var errors = Seq[FormError]()

    if (data.audience.contains("Public")) {
      Right(Audience.Public)
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
        allDepts.find(_.code == code)
      }
      val deptComponent = department match {
        case Some(d) if deptComponentValues.nonEmpty => Some(DepartmentAudience(d.code, deptComponentValues))
        case Some(d) =>
          errors :+= FormError("department", "error.audience.noDepartmentSubsets")
          None
        case None if data.department.isDefined =>
          errors :+= FormError("department", "error.department.invalid")
          None
        case None => None // No department audience - no problem
      }

      if (department.isEmpty && globalComponents.isEmpty) {
        errors :+= FormError("audience", "error.audience.empty")
      }

      if (errors.nonEmpty) {
        Left(errors)
      } else {
        Right(Audience(globalComponents ++ deptComponent))
      }
    }
  }
}
