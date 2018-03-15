package models

case class MessageState(
  dbValue: String
)

object MessageState {

  val Available = MessageState("A")
  val Taken = MessageState("T")
  val Success = MessageState("S")
  val Failure = MessageState("F")
  val Skipped = MessageState("O")
  val Muted = MessageState("M")

  private val values = Set(Available, Taken, Success, Failure, Skipped, Muted)

  def parse(dbValue: String): MessageState = values.find(_.dbValue == dbValue).getOrElse(throw new IllegalArgumentException(dbValue))

}
