package models.publishing

import enumeratum.{Enum, EnumEntry}
import models.publishing.Ability._

trait Role {
  def can(ability: Ability): Boolean

  def can(abilities: Seq[Ability]): Boolean = abilities.forall(can)

  def cannot(ability: Ability): Boolean = !can(ability)

  def cannot(abilities: Seq[Ability]): Boolean = !can(abilities)
}

sealed trait PublishingRole extends EnumEntry with Role

object PublishingRole extends Enum[PublishingRole] {
  private type V = PublishingRole

  val values = findValues

  case object NewsManager extends RoleWithAbilities(CreateNews, EditNews, ViewNews) with V
  case object NotificationsManager extends RoleWithAbilities(CreateNotifications, ViewNotifications) with V
  case object APINotificationsManager extends RoleWithAbilities(CreateAPINotifications) // God-like, not limited by Audience scope
  case object Viewer extends RoleWithAbilities(ViewNews, ViewNotifications) with V
}

case class CompoundRole(roles: Seq[Role]) extends Role {
  def can(ability: Ability) = roles.exists(_.can(ability))
}

object NullRole extends Role {
  def can(ability: Ability) = false
}