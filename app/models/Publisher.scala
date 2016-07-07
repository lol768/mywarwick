package models

import warwick.sso.{User, Usercode}

import scala.concurrent.Future

import enumeratum._

/**
  *
  */
case class Publisher(
  id: String,
  name: String,
  departmentCode: Option[String]
)

// Publisher has-many PublisherPermission
case class PublisherPermission(
  usercode: Usercode,
  value: Nothing // some value for permission level ???
)

case class PermissionResult(
  scope: PermissionScope
)


object Permission {
  sealed trait Role extends EnumEntry
  object Role extends Enum[Role] {
    val values = findValues
    // Can publish, edit, delete, news items
    case object NewsManager extends Role
    //
    case object NotificationsManager extends Role
    // Can read and report on news and notifications
    case object Reporter extends Role
  }
}



sealed trait PermissionLevel extends EnumEntry
object PermissionLevel extends Enum[PermissionLevel] {
  private type V = PermissionLevel
  val values = findValues
  case object PostNews extends V
  case object EditNews extends V
  case object DeleteNews extends V
  case object PostNotification extends V
  case object EditNotification extends V
}

sealed trait PermissionScope
object AllDepartments extends PermissionScope
case class DepartmentList(departments: Seq[String]) extends PermissionScope

/**
  * SQL
  *
  * PUBLISHER(id,name,department_code)
  * PUBLISHER_PERMISSION(provider_id, usercode, value)
  */

// FIXME this is for development, do not merge
trait DreamService {
  def permissionsCheck(user: User): Future[PermissionResult] = ???

}



