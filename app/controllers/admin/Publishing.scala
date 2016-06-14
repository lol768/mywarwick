package controllers.admin

import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import services.PublishCategoryService
import services.dao.DepartmentInfoDao

case class Publish[A](item: A, audience: Seq[String], department: Option[String], categoryIds: Seq[String]) extends AudienceFormData

trait Publishing[A] extends DepartmentOptions with CategoryOptions {

  def publishForm(itemMapping: Mapping[A]): Form[Publish[A]] =
    Form(mapping(
      "item" -> itemMapping,
      "audience" -> seq(nonEmptyText),
      "department" -> optional(text),
      "categories" -> seq(nonEmptyText)
        .verifying("You must select at least one category", _.nonEmpty)
        // Rationale: after removing all possible options, anything that remains is invalid
        .verifying("Some selections were invalid", _.diff(categoryOptions.map(_.id)).isEmpty)
    )(Publish.apply)(Publish.unapply))

}

trait DepartmentOptions {

  val departmentInfoDao: DepartmentInfoDao

  implicit val executionContext = system.ThreadPools.web

  private val departmentTypes = Set("ACADEMIC", "SERVICE")
  private val departmentInitialValue = Seq("" -> "--- Department ---")

  def departmentOptions =
    departmentInfoDao.allDepartments
      .recover { case e => Nil }
      .map { depts =>
        departmentInitialValue ++ depts.filter { info => departmentTypes.contains(info.`type`) }
          .sortBy { info => info.name }
          .map { info => info.code -> info.name }
      }

}

trait CategoryOptions {

  val publishCategoryService: PublishCategoryService

  lazy val categoryOptions = publishCategoryService.all()

}
