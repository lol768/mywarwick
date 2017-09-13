package services.messaging

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}

import scala.collection.JavaConverters._
import scala.util.Try

trait SmsSendingService {

  def configuration: Configuration

  private val requestConfig: RequestConfig = RequestConfig.custom()
    .setConnectTimeout(SmsOutputService.connectTimeout.toMillis.toInt)
    .setSocketTimeout(SmsOutputService.socketTimeout.toMillis.toInt)
    .build()

  private val client: CloseableHttpClient = HttpClientBuilder.create()
    .setDefaultRequestConfig(requestConfig)
    .build()

  private val baseUrlOption: Option[String] = configuration.getString("sms.baseUrl")
  private val usernameOption: Option[String] = configuration.getString("sms.username")
  private val passwordOption: Option[String] = configuration.getString("sms.password")

  def sendSms(phoneNumber: PhoneNumber, message: String): Try[JsObject] = {
    val baseUrl = baseUrlOption.getOrElse(throw new IllegalArgumentException("sms.baseUrl not defined"))
    val username = usernameOption.getOrElse(throw new IllegalArgumentException("sms.username not defined"))
    val password = passwordOption.getOrElse(throw new IllegalArgumentException("sms.password not defined"))

    val request = new HttpPost(baseUrl)
    request.setEntity(new UrlEncodedFormEntity(Seq(
      new BasicNameValuePair("username", username),
      new BasicNameValuePair("password", password),
      new BasicNameValuePair("number", PhoneNumberUtil.getInstance.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL)),
      new BasicNameValuePair("message", message)
    ).asJava))

    var response: CloseableHttpResponse = null

    val result = Try {
      response = client.execute(request)
      val entity = response.getEntity
      Json.parse(EntityUtils.toString(entity)).as[JsObject]
    }

    if (response != null) response.close()

    result
  }

}
