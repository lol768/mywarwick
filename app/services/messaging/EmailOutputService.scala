package services.messaging

import javax.inject.{Inject, Named}

import actors.MessageProcessing.ProcessingResult
import models.{Activity, DateFormats, MessageSend}
import play.api.Configuration
import play.api.libs.mailer.{Email, MailerClient}
import services.UserPreferencesService
import system.EmailSanitiser
import warwick.sso.User

import scala.concurrent.Future

@Named("email")
class EmailOutputService @Inject() (
  mailer: MailerClient,
  config: Configuration,
  userPreferencesService: UserPreferencesService
) extends OutputService {

  import system.ThreadPools.email

  val from = config.getString("mywarwick.mail.notifications.from")
    .orElse(config.getString("mywarwick.mail.from"))
    .getOrElse(throw new IllegalStateException("No From address - set mywarwick.mail[.notifications].from"))

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
    val date = DateFormats.emailDateTime(activity.publishedAt)
    val rootUrl = config.getString("mywarwick.rootUrl")
    require(rootUrl.nonEmpty, "Valid mywarwick.rootUrl not present")

    val optOutRoute = s"${controllers.routes.HomeController.settings().url}"
    val baseUrl = s"${rootUrl.get}"
    val loggedInBefore = userPreferencesService.exists(user.usercode)
    Email(
      subject = EmailSanitiser.sanitiseUserInputForHeader(activity.title),
      from = from,
      to = Seq(fullAddress),
      bodyText = Some(views.txt.email(user, activity, date, baseUrl, optOutRoute, loggedInBefore).body),
      bodyHtml = Some(views.html.email(user, activity, date, baseUrl, optOutRoute, loggedInBefore).body)
    )
  }
}
