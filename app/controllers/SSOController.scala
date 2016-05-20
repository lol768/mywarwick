package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.mvc.{Action, Cookie, DiscardingCookie}
import uk.ac.warwick.sso.client.SSOClientHandlerImpl.GLOBAL_LOGIN_COOKIE_NAME
import uk.ac.warwick.sso.client.SSOToken.SSC_TICKET_TYPE
import uk.ac.warwick.sso.client.cache.UserCache
import uk.ac.warwick.sso.client.{SSOConfiguration, SSOToken}
import uk.ac.warwick.util.core.StringUtils
import warwick.sso.{LoginContext, SSOClient}
import java.net.URL

import uk.ac.warwick.sso.client.core.LinkGenerator

/**
  * This is some weird SSO stuff for Start, while we're still working out
  * how best to handle indefinite sessions. It likely will apply to any similar
  * Play app but DON'T copy and paste this into Search. Better to move it into
  * SSO Client as an optional behaviour.
  */
@Singleton
class SSOController @Inject()(
  ssoConfig: SSOConfiguration,
  userCache: UserCache,
  ssoClient: SSOClient
) extends BaseController {

  import ssoClient.Lenient

  val SSC_NAME = ssoConfig.getString("shire.sscookie.name")
  val SSC_PATH = ssoConfig.getString("shire.sscookie.path")
  val SSC_DOMAIN = ssoConfig.getString("shire.sscookie.domain")

  /**
    * Returns true if we should redirect to Websignon to refresh the session.
    * False if we're fine where we are.
    *
    * Normal SSOClient will refresh if you have an LTC but no SSC cookie, because
    * we set the cookies to expire at the same time as the session. We are expecting
    * the SSC to hang around forever, so instead we check that our local cached copy
    * of the user's session is missing (meaning it probably expired away).
    */
  def info = Lenient.disallowRedirect { request =>
    val ltc = request.cookies.get(GLOBAL_LOGIN_COOKIE_NAME).filter(hasValue)
    val ssc = request.cookies.get(SSC_NAME).filter(hasValue)

    val refresh = ssc.exists(tokenNotInUserCache) || (ssc.isEmpty && ltc.nonEmpty)

    val links = ssoClient.linkGenerator(request)
    links.setTarget(s"https://${request.host}")

    val loginUrl = links.getLoginUrl
    val logoutUrl = s"https://${request.host}/logout?target=https://${request.host}"

    Ok(Json.obj(
      "refresh" -> (if (refresh) loginUrl else false),
      "user" -> contextUserInfo(request.context),
      "links" -> Json.obj(
        "login" -> loginUrl,
        "logout" -> logoutUrl
      )
    ))
  }

  def logout = Action { request =>
    val host = request.getQueryString("target").get
    val links = ssoClient.linkGenerator(request)
    links.setTarget(host)
    val redirect = Redirect(s"${links.getLogoutUrl}")
    redirect.discardingCookies(DiscardingCookie(SSC_NAME, SSC_PATH, Some(SSC_DOMAIN)))
  }

  private def contextUserInfo(context: LoginContext) =
    context.user.map(user =>
      Json.obj(
        "authenticated" -> true,
        "usercode" -> user.usercode.string,
        "name" -> user.name.full,
        "masquerading" -> context.isMasquerading
      )
    ).getOrElse(
      Json.obj(
        "authenticated" -> false
      )
    )

  private def hasValue(cookie: Cookie): Boolean =
    StringUtils.hasText(cookie.value)

  private def tokenNotInUserCache(cookie: Cookie): Boolean =
    userCache.get(new SSOToken(cookie.value, SSC_TICKET_TYPE)) == null

}
