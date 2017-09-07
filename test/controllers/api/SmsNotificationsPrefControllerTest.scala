package controllers.api

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import helpers.WithActorSystem
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsBoolean, JsNull, JsString, Json}
import play.api.test.FakeRequest
import services.{MockNavigationService, SecurityService, SecurityServiceImpl, SmsNotificationsPrefService}
import play.api.test.Helpers._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.cache.CacheApi
import warwick.sso._

class SmsNotificationsPrefControllerTest extends PlaySpec with MockitoSugar with WithActorSystem {

  private val ron = Users.create(usercode = Usercode("ron"))

  private val mockSSOClient = new MockSSOClient(new LoginContext {
    override val user: Option[User] = Some(ron)
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = "https://app.example.com/login"
    override def userHasRole(role: RoleName) = false
    override def actualUserHasRole(role: RoleName) = false
  })
  val securityService = new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], mock[CacheApi])

  trait Fixture {
    protected val smsNotificationsPrefService: SmsNotificationsPrefService = mock[SmsNotificationsPrefService]
    protected val controller = new SmsNotificationsPrefController(
      securityService,
      smsNotificationsPrefService
    ) {
      override val navigationService = new MockNavigationService()
      override val ssoClient: MockSSOClient = mockSSOClient
    }
  }

  "SmsNotificationsPrefController#update" should {

    "validate invalid phone number" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> true,
        "smsNumber" -> "Hello"
      )
      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe BAD_REQUEST
      verify(smsNotificationsPrefService, never()).set(any[Usercode], any[Boolean])
      verify(smsNotificationsPrefService, never()).setNumber(any[Usercode], any[Option[PhoneNumber]])
    }

    "validate enabled with empty number" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> true,
        "smsNumber" -> ""
      )
      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe BAD_REQUEST
      verify(smsNotificationsPrefService, never()).set(any[Usercode], any[Boolean])
      verify(smsNotificationsPrefService, never()).setNumber(any[Usercode], any[Option[PhoneNumber]])
    }

    "unset phone number with null" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> false,
        "smsNumber" -> JsNull
      )
      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe OK
      verify(smsNotificationsPrefService, times(1)).set(ron.usercode, wantsSMS = false)
      verify(smsNotificationsPrefService, times(1)).setNumber(ron.usercode, None)
    }

    "unset phone number with empty string" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> false,
        "smsNumber" -> ""
      )
      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe OK
      verify(smsNotificationsPrefService, times(1)).set(ron.usercode, wantsSMS = false)
      verify(smsNotificationsPrefService, times(1)).setNumber(ron.usercode, None)
    }

    "valid new phone number need verification" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> true,
        "smsNumber" -> "+44 7773 12 55 77"
      )
      private val parsedPhoneNumber = PhoneNumberUtil.getInstance.parse("+44 7773 12 55 77", "GB")
      when(smsNotificationsPrefService.getNumber(ron.usercode)).thenReturn(None)
      when(smsNotificationsPrefService.getVerificationCode(ron.usercode)).thenReturn(None)
      when(smsNotificationsPrefService.requireVerification(ron.usercode, parsedPhoneNumber)).thenReturn(true)

      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe OK
      (contentAsJson(result) \ "status").get mustBe JsString("verificationRequired")
      verify(smsNotificationsPrefService, times(0)).set(ron.usercode, wantsSMS = true)
      verify(smsNotificationsPrefService, times(0)).setNumber(ron.usercode, Some(parsedPhoneNumber))
    }

    "valid changed phone number needs verification" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> true,
        "smsNumber" -> "+44 7773 12 55 77"
      )
      private val parsedPhoneNumber = PhoneNumberUtil.getInstance.parse("+44 7773 12 55 77", "GB")
      private val parsedCurrentPhoneNumber = PhoneNumberUtil.getInstance.parse("+44 7773 12 55 78", "GB")
      when(smsNotificationsPrefService.getNumber(ron.usercode)).thenReturn(Some(parsedCurrentPhoneNumber))
      when(smsNotificationsPrefService.getVerificationCode(ron.usercode)).thenReturn(None)
      when(smsNotificationsPrefService.requireVerification(ron.usercode, parsedPhoneNumber)).thenReturn(true)

      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe OK
      (contentAsJson(result) \ "status").get mustBe JsString("verificationRequired")
      verify(smsNotificationsPrefService, times(0)).set(ron.usercode, wantsSMS = true)
      verify(smsNotificationsPrefService, times(0)).setNumber(ron.usercode, Some(parsedPhoneNumber))
    }

    "valid phone number verification code sent no code" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> true,
        "smsNumber" -> "+44 7773 12 55 77"
      )
      private val parsedPhoneNumber = PhoneNumberUtil.getInstance.parse("+44 7773 12 55 77", "GB")
      when(smsNotificationsPrefService.getNumber(ron.usercode)).thenReturn(None)
      when(smsNotificationsPrefService.getVerificationCode(ron.usercode)).thenReturn(Some("123456"))

      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "status").get mustBe JsString("verificationRequired")
      verify(smsNotificationsPrefService, times(0)).set(ron.usercode, wantsSMS = true)
      verify(smsNotificationsPrefService, times(0)).setNumber(ron.usercode, Some(parsedPhoneNumber))
      verify(smsNotificationsPrefService, times(0)).requireVerification(ron.usercode, parsedPhoneNumber)
    }

    "valid phone number verification code sent wrong code" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> true,
        "smsNumber" -> "+44 7773 12 55 77",
        "verificationCode" -> "nope"
      )
      private val parsedPhoneNumber = PhoneNumberUtil.getInstance.parse("+44 7773 12 55 77", "GB")
      when(smsNotificationsPrefService.getNumber(ron.usercode)).thenReturn(None)
      when(smsNotificationsPrefService.getVerificationCode(ron.usercode)).thenReturn(Some("123456"))

      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "status").get mustBe JsString("verificationRequired")
      verify(smsNotificationsPrefService, times(0)).set(ron.usercode, wantsSMS = true)
      verify(smsNotificationsPrefService, times(0)).setNumber(ron.usercode, Some(parsedPhoneNumber))
      verify(smsNotificationsPrefService, times(0)).requireVerification(ron.usercode, parsedPhoneNumber)
    }

    "valid phone number verification code sent correct code" in new Fixture {
      private val body = Json.obj(
        "wantsSms" -> true,
        "smsNumber" -> "+44 7773 12 55 77",
        "verificationCode" -> "123456"
      )
      private val parsedPhoneNumber = PhoneNumberUtil.getInstance.parse("+44 7773 12 55 77", "GB")
      when(smsNotificationsPrefService.getNumber(ron.usercode)).thenReturn(None)
      when(smsNotificationsPrefService.getVerificationCode(ron.usercode)).thenReturn(Some("123456"))

      private val result = call(controller.update, FakeRequest("POST", "/").withBody(body))
      status(result) mustBe OK
      verify(smsNotificationsPrefService, times(1)).set(ron.usercode, wantsSMS = true)
      verify(smsNotificationsPrefService, times(1)).setNumber(ron.usercode, Some(parsedPhoneNumber))
      verify(smsNotificationsPrefService, times(0)).requireVerification(ron.usercode, parsedPhoneNumber)
    }

  }

}
