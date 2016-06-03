package controllers.admin

import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import services.dao.DepartmentInfoDao

case class Publish[A](item: A, audience: Seq[String], department: Option[String]) extends AudienceFormData

trait Publishing[A] extends DepartmentOptions {

  def publishForm(itemMapping: Mapping[A]): Form[Publish[A]] =
    Form(mapping(
      "item" -> itemMapping,
      "audience" -> seq(nonEmptyText),
      "department" -> optional(text)
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
