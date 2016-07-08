package models.news

trait PermissionLevel
object PermissionLevel {
  case object Contributor extends PermissionLevel
  case object Admin extends PermissionLevel
}
