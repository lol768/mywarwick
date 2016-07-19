package models.publishing

import enumeratum.EnumEntry

class RoleWithAbilities(abilities: Ability*) extends EnumEntry with Role {
  override def can(ability: Ability) = abilities.contains(ability)
}
