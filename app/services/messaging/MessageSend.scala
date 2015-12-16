package services.messaging

import anorm.SqlParser._
import anorm.~
import org.joda.time.DateTime
import warwick.sso.Usercode

/**
  * Database representation of a message that is due to be sent,
  * to a user about an activity to a single output.
  */
case class MessageSend(
  id: String,
  activityID: String,
  usercode: Usercode,
  output: Output,
  updatedAt: DateTime
)

object MessageSend {
  val rowParser =
    get[String]("id") ~
    get[String]("notification_id") ~
    get[String]("usercode") ~
    get[String]("output") ~
    get[DateTime]("updated_at") map {
      case id ~ activity ~ usercode ~ output ~ updatedAt =>
        MessageSend(id, activity, Usercode(usercode), Output.parse(output), updatedAt)
    }
}
