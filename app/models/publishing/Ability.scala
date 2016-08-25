package models.publishing

import enumeratum.{EnumEntry, Enum}

sealed trait Ability extends EnumEntry

object Ability extends Enum[Ability] {
  private type V = Ability
  val values = findValues

  case object CreateNews extends V
  case object EditNews extends V
  case object DeleteNews extends V
  case object CreateNotifications extends V
  case object EditNotifications extends V
  case object DeleteNotifications extends V
  case object CreateAPINotifications extends V
  case object ViewNews extends V
  case object ViewNotifications extends V
}