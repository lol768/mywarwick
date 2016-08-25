package models

object AudienceSize {
  object Public extends AudienceSize

  case class Finite(count: Int) extends AudienceSize
}

sealed trait AudienceSize