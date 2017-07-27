package services.messaging

import actors.MessageProcessing.ProcessingResult
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.inject.Inject
import com.google.inject.name.Named
import models.MessageSend
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import play.api.Configuration
import play.api.libs.json.{JsBoolean, JsObject, Json}
import services.SmsNotificationsPrefService

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

object SmsOutputService {
  val connectTimeout: FiniteDuration = 5.seconds
  val socketTimeout: FiniteDuration = 5.seconds

  private val messageSuffix: String = "\nTo opt-out visit my.warwick.ac.uk/settings"
  private val maxMessageLength = 160 - messageSuffix.length
  def formatForSms(message: String): String = {
    if (message.length > maxMessageLength) {
      message.substring(0, maxMessageLength - 3) + "..." + messageSuffix
    } else {
      message + messageSuffix
    }
  }
}

@Named("sms")
class SmsOutputService @Inject()(
  configuration: Configuration,
  smsNotificationsPrefService: SmsNotificationsPrefService
) extends OutputService {

  import system.ThreadPools.sms

  val requestConfig: RequestConfig = RequestConfig.custom()
    .setConnectTimeout(SmsOutputService.connectTimeout.toMillis.toInt)
    .setSocketTimeout(SmsOutputService.socketTimeout.toMillis.toInt)
    .build()

  val client: CloseableHttpClient = HttpClientBuilder.create()
    .setDefaultRequestConfig(requestConfig)
    .build()

  val baseUrlOption: Option[String] = configuration.getString("sms.baseUrl")
  val usernameOption: Option[String] = configuration.getString("sms.username")
  val passwordOption: Option[String] = configuration.getString("sms.password")

  override def send(message: MessageSend.Heavy): Future[ProcessingResult] = {
    val usercode = message.user.usercode

    val smsEnabled = smsNotificationsPrefService.get(usercode)
    val smsNumberOption = smsNotificationsPrefService.getNumber(usercode)

    if (!smsEnabled) {
      Future.successful(ProcessingResult(success = false, s"SMS disabled for $usercode"))
    } else if (smsNumberOption.isEmpty) {
      Future.successful(ProcessingResult(success = false, s"No phone number defined for $usercode"))
    } else {

      val baseUrl = baseUrlOption.getOrElse(throw new IllegalArgumentException("sms.baseUrl not defined"))
      val username = usernameOption.getOrElse(throw new IllegalArgumentException("sms.username not defined"))
      val password = passwordOption.getOrElse(throw new IllegalArgumentException("sms.password not defined"))

      Future {
        val request = new HttpPost(baseUrl)
        request.setEntity(new UrlEncodedFormEntity(Seq(
          new BasicNameValuePair("username", username),
          new BasicNameValuePair("password", password),
          new BasicNameValuePair("number", PhoneNumberUtil.getInstance.format(smsNumberOption.get, PhoneNumberFormat.INTERNATIONAL)),
          new BasicNameValuePair("message", SmsOutputService.formatForSms(message.activity.title))
        ).asJava))

        var response: CloseableHttpResponse = null

        val processingResult = Try {
          response = client.execute(request)
          val entity = response.getEntity
          val json = Json.parse(EntityUtils.toString(entity)).as[JsObject]
          val result = json.value("success").as[JsBoolean].value
          if (result) {
            ProcessingResult(success = true, s"SMS message sent to $usercode")
          } else {
            ProcessingResult(success = false, s"Failed sending SMS message sent to $usercode: ${json.toString}")
          }
        }.recover {
          case e => ProcessingResult(success = false, s"Failed sending SMS message sent to $usercode: ${e.getMessage}")
        }

        if (response != null) response.close()

        processingResult.get
      }
    }
  }
}
