package controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{Cookie, Action}
import uk.ac.warwick.sso.client.{SSOToken, SSOConfiguration}
import uk.ac.warwick.sso.client.cache.UserCache
import uk.ac.warwick.sso.client.SSOClientHandlerImpl._
import uk.ac.warwick.util.core.StringUtils
import warwick.sso.SSOClient

/**
  * This is some weird SSO stuff for Start, while we're still working out
  * how best to handle indefinite sessions. It likely will apply to any similar
  * Play app but DON'T copy and paste this into Search. Better to move it into
  * SSO Client as an optional behaviour.
  */
class SSOController @Inject()(
  ssoConfig: SSOConfiguration,
  userCache: UserCache
) extends BaseController {

  val SERVICE_SPECIFIC_COOKIE_NAME = ssoConfig.getString("shire.sscookie.name")

  /**
    * Returns true if we should redirect to Websignon to refresh the session.
    * False if we're fine where we are.
    *
    * Normal SSOClient will refresh if you have an LTC but no SSC cookie, because
    * we set the cookies to expire at the same time as the session. We are expecting
    * the SSC to hang around forever, so instead we check that our local cached copy
    * of the user's session is missing (meaning it probably expired away).
    */
  def ssotest = Action { request =>
    val ssc: Option[Cookie] = request.cookies.get(SERVICE_SPECIFIC_COOKIE_NAME)
    val refresh: Boolean = ssc.filter {
      cookie => StringUtils.hasText(cookie.value)
    }.exists { cookie =>
      userCache.get(new SSOToken(cookie.value, SSOToken.SSC_TICKET_TYPE)) == null
    }

    Ok(Json.toJson(refresh))
  }

}
