package models

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime
import warwick.sso.{User, Usercode}

/**
  * Database representation of a message that is due to be sent,
  * to a user about an activity to a single output.
  */
case class MessageSend[U, A](
  id: String,
  activity: A,
  user: U,
  output: Output,
  updatedAt: DateTime
) {
  /** Convert a Light to a Heavy */
  def fill(user: User, activity: Activity) = this.copy(user=user, activity=activity)
}

object MessageSend {
  import warwick.anorm.converters.ColumnConversions._

  type Light = MessageSend[Usercode, String]
  type Heavy = MessageSend[User, Activity]

  val rowParser: RowParser[MessageSend.Light] =
    get[String]("id") ~
    get[String]("activity_id") ~
    get[String]("usercode").map(Usercode.apply) ~
    get[String]("output").map(Output.parse) ~
    get[DateTime]("updated_at") map {
      case id ~ activity ~ usercode ~ output ~ updatedAt =>
        MessageSend(id, activity, usercode, output, updatedAt)
    }
}
