package models

import org.joda.time.DateTime

case class Notification(
                         id: String,
                         providerId: String,
                         notificationType: String,
                         title: String,
                         text: String,
                         replacedBy: String,
                         createdAt: DateTime
                         )

case class IncomingNotification(
                         providerId: String,
                         notificationType: String,
                         title: String,
                         text: String,
                         scopes: Seq[String],
                         replace: Boolean
                         )

object Notification {

}

