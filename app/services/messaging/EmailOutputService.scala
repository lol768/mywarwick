package services.messaging

import javax.inject.{Inject, Named}

import actors.MessageProcessing.ProcessingResult
import models.{DateFormats, Activity}
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
    val date = DateFormats.emailDateTime(activity.generatedAt)
    Email(
      subject = activity.title,
      from = from,
      to = Seq(fullAddress),
      bodyText = Some(views.txt.email(user, activity, date).body)
    )
  }
}
