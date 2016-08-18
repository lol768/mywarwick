package models

trait Hit {
  def attributes: Map[String, Seq[String]]
}

case class EventHit(
  category: String,
  action: String,
  label: Option[String] = None,
  value: Option[Int] = None
) extends Hit {
  val attributes = Map(
    "t" -> Seq("event"),
    "ec" -> Seq(category),
    "ea" -> Seq(action),
    "el" -> label.toSeq,
    "ev" -> value.map(_.toString).toSeq
  )
}
