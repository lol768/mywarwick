package models

case class QueueStatus(
  state: MessageState,
  output: Output,
  count: Int
)

