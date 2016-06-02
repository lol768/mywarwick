package models.news

object AudienceSize {
  object Public extends AudienceSize

  case class Finite(count: Int) extends AudienceSize
}

trait AudienceSize