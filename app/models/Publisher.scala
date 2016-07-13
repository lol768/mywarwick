package models

import enumeratum._
import warwick.sso.Usercode

case class Publisher(
  id: String,
  name: String
)

case class PublisherPermission(
  usercode: Usercode,
  role: PublishingRole
)

sealed trait PublishingAbility extends EnumEntry

object PublishingAbility extends Enum[PublishingAbility] {
  private type V = PublishingAbility
  val values = findValues

  case object CreateNews extends V
  case object EditNews extends V
  case object CreateNotifications extends V
  case object ViewNews extends V
  case object ViewNotifications extends V
}

sealed trait PublishingRole extends EnumEntry {
  def can(permission: PublishingAbility): Boolean
}

object PublishingRole extends Enum[PublishingRole] {
  import PublishingAbility._

  class UserWhoCan(permissions: PublishingAbility*) extends PublishingRole {
    override def can(permission: PublishingAbility) = permissions.contains(permission)
  }

  val values = findValues

  case object NewsManager extends UserWhoCan(CreateNews, EditNews, ViewNews)
  case object NotificationsManager extends UserWhoCan(CreateNotifications, ViewNotifications)
  case object Viewer extends UserWhoCan(ViewNews, ViewNotifications)
}

// TODO: Permissions for publishing to departments
sealed trait PermissionScope
object PermissionScope {
  object AllDepartments extends PermissionScope
  case class Departments(departments: Seq[String]) extends PermissionScope
}

