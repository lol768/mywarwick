package models.publishing

sealed trait PermissionScope

object PermissionScope {
  object AllDepartments extends PermissionScope
  case class Departments(departments: Seq[String]) extends PermissionScope
}