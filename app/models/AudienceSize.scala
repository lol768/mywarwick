package models

object AudienceSize {
  object Public extends AudienceSize {
    val toOption: Option[Int] = None
  }

  case class Finite(count: Int) extends AudienceSize {
    val toOption: Option[Int] = Some(count)
  }

  def fromOption(option: Option[Int]): AudienceSize = option match {
    case Some(count) => Finite(count)
    case _ => Public
  }
}

sealed trait AudienceSize {
  val toOption: Option[Int]
}