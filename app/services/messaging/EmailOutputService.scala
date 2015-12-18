package services.messaging

import javax.inject.{Inject, Named}

import actors.MessageProcessing.ProcessingResult
import models.Activity
import play.api.Configuration
import play.api.libs.mailer.{Email, MailerClient}
import warwick.sso.User

import scala.concurrent.Future

/**
  *
  */
@Named("email")
class EmailOutputService @Inject() (
  mailer: MailerClient,
  config: Configuration
) extends OutputService {

  import system.ThreadPools.email

  val from = config.getString("start.mail.notifications.from")
            .orElse(config.getString("start.mail.from"))
            .getOrElse(throw new IllegalStateException("No From address - set start.mail[.notifications].from"))

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = Future {
    import message.{user, activity}
    user.email.map { address =>
      val email = build(address, user, activity)
      val id = mailer.send(email)
      ProcessingResult(success = true, message = s"Sent email ${id} to user ${user.usercode.string}")
    }.getOrElse {
      ProcessingResult(success = false, message = s"No email address for user ${user.usercode.string}")
    }
  }

  def build(address: String, user: User, activity: Activity): Email = {
    val fullAddress = user.name.full.map(full => s"${full} <${address}>").getOrElse(address)
    Email(
      subject = activity.title,
      from = from,
      to = Seq(fullAddress),
      bodyText = Some(
        s""" You have a new notification.
            | ---
            |
            | ${activity.text}
            |
            | ---
            | This notification was generated automatically by Warwick University.
            """.stripMargin)
    )
  }
}
