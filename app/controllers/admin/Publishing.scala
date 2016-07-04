package controllers.admin

import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import services.NewsCategoryService
import services.dao.{DepartmentInfo, DepartmentInfoDao}

case class Publish[A](item: A, audience: Seq[String], department: Option[String], categoryIds: Seq[String], ignoreCategories: Boolean) extends AudienceFormData

trait Publishing[A] extends DepartmentOptions with CategoryOptions {

  def publishForm(
    categoriesRequired: Boolean,
    itemMapping: Mapping[A]
  ): Form[Publish[A]] =
    Form(mapping(
      "item" -> itemMapping,
      "audience" -> seq(nonEmptyText),
      "department" -> optional(text),
      "categories" -> seq(nonEmptyText)
        .verifying("You must select at least one category", categorySelected(categoriesRequired) _)
        // Rationale: after removing all possible options, anything that remains is invalid
        .verifying("Some selections were invalid", _.diff(categoryOptions.map(_.id)).isEmpty),
      "ignoreCategories" -> boolean
    )(Publish.apply)(Publish.unapply))

  private def categorySelected(enabled: Boolean)(ids: Seq[String]): Boolean =
    if (enabled) ids.nonEmpty else true

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
