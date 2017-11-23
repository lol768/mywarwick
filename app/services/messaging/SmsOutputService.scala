package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.inject.Inject
import com.google.inject.name.Named
import models.MessageSend
import play.api.Configuration
import play.api.libs.json.JsBoolean
import services.SmsNotificationsPrefService

import scala.concurrent.Future
import scala.concurrent.duration._

object SmsOutputService {
  val connectTimeout: FiniteDuration = 5.seconds
  val socketTimeout: FiniteDuration = 5.seconds
}

@Named("sms")
class SmsOutputService @Inject()(
  val configuration: Configuration,
  smsNotificationsPrefService: SmsNotificationsPrefService
) extends OutputService with SmsSendingService {

  import system.ThreadPools.sms

  private val rootDomain: String = configuration.get[Option[String]]("mywarwick.rootDomain").getOrElse("my.warwick.ac.uk")
  private val messageSuffix: String = s"\nTo opt-out visit $rootDomain/settings"
  private val maxMessageLength = 160 - messageSuffix.length
  def formatForSms(message: String): String = {
    if (message.length > maxMessageLength) {
      message.substring(0, maxMessageLength - 3) + "..." + messageSuffix
    } else {
      message + messageSuffix
    }
  }

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    val usercode = message.user.usercode

    val smsEnabled = smsNotificationsPrefService.get(usercode)
    val smsNumberOption = smsNotificationsPrefService.getNumber(usercode)

    if (!smsEnabled) {
      Future.successful(ProcessingResult(success = false, s"SMS disabled for $usercode"))
    } else if (smsNumberOption.isEmpty) {
      Future.successful(ProcessingResult(success = false, s"No phone number defined for $usercode"))
    } else {
      Future {
        sendSms(smsNumberOption.get, formatForSms(message.activity.title)).map(json => {
          val result = json.value("success").as[JsBoolean].value
          if (result) {
            ProcessingResult(success = true, s"SMS message sent to $usercode")
          } else {
            ProcessingResult(success = false, s"Failed sending SMS message sent to $usercode: ${json.toString}")
          }
        }).recover {
          case e => ProcessingResult(success = false, s"Failed sending SMS message sent to $usercode: ${e.getMessage}")
        }.get
      }
    }
  }
}
