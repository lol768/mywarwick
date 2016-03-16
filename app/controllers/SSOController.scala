package controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{Action, Cookie}
import uk.ac.warwick.sso.client.SSOClientHandlerImpl.GLOBAL_LOGIN_COOKIE_NAME
import uk.ac.warwick.sso.client.SSOToken.SSC_TICKET_TYPE
import uk.ac.warwick.sso.client.cache.UserCache
import uk.ac.warwick.sso.client.{SSOConfiguration, SSOToken}
import uk.ac.warwick.util.core.StringUtils

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
    val ltc = request.cookies.get(GLOBAL_LOGIN_COOKIE_NAME).filter(hasValue)
    val ssc = request.cookies.get(SERVICE_SPECIFIC_COOKIE_NAME).filter(hasValue)

    val refresh = ssc.exists(tokenNotInUserCache) || ssc.isEmpty && ltc.isDefined

    Ok(Json.toJson(refresh))
  }

  private def hasValue(cookie: Cookie): Boolean =
    StringUtils.hasText(cookie.value)

  private def tokenNotInUserCache(cookie: Cookie): Boolean =
    userCache.get(new SSOToken(cookie.value, SSC_TICKET_TYPE)) == null

}
