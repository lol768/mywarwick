package controllers.admin

import play.api.data.Forms._
import play.api.data.Mapping
import services.NewsCategoryService
import services.dao.{DepartmentInfo, DepartmentInfoDao}

trait Publishing extends DepartmentOptions with CategoryOptions {

  val categoryMapping: Mapping[Seq[String]] =
    seq(nonEmptyText)
      .verifying("You must select at least one category", _.nonEmpty)
      // Rationale: after removing all possible options, anything that remains is invalid
      .verifying("Some selections were invalid", _.diff(categoryOptions.map(_.id)).isEmpty)

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

  lazy val categoryOptions = newsCategoryService.all()

}
