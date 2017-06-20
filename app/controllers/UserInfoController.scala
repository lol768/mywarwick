package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, Cookie, DiscardingCookie}
import services.analytics.AnalyticsMeasurementService
import services.{PhotoService, UserInitialisationService}
import system.ThreadPools.externalData
import uk.ac.warwick.sso.client.SSOClientHandlerImpl.GLOBAL_LOGIN_COOKIE_NAME
import uk.ac.warwick.sso.client.SSOToken.SSC_TICKET_TYPE
import uk.ac.warwick.sso.client.cache.UserCache
import uk.ac.warwick.sso.client.{SSOConfiguration, SSOToken}
import uk.ac.warwick.util.core.StringUtils
import warwick.sso.{LoginContext, SSOClient}

import scala.concurrent.Future

/**
  * This is some weird SSO stuff for My Warwick, while we're still working out
  * how best to handle indefinite sessions. It likely will apply to any similar
  * Play app but DON'T copy and paste this into Search. Better to move it into
  * SSO Client as an optional behaviour.
  */
@Singleton
class UserInfoController @Inject()(
  ssoConfig: SSOConfiguration,
  userCache: UserCache,
  ssoClient: SSOClient,
  userInitialisationService: UserInitialisationService,
  photoService: PhotoService,
  measurementService: AnalyticsMeasurementService
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
  def info = Lenient.disallowRedirect.async { implicit request =>
    val ltc = request.cookies.get(GLOBAL_LOGIN_COOKIE_NAME).filter(hasValue)
    val ssc = request.cookies.get(SSC_NAME).filter(hasValue)

    val refresh = ssc.exists(tokenNotInUserCache) || (ssc.isEmpty && ltc.nonEmpty)

    val links = ssoClient.linkGenerator(request)
    links.setTarget(s"https://${request.host}")

    val loginUrl = links.getLoginUrl
    val logoutUrl = s"https://${request.host}/logout?target=https://${request.host}"

    request.context.user.map(_.usercode)
      .foreach(userInitialisationService.maybeInitialiseUser)

    contextUserInfo(request.context).map { userInfo =>
      val token = requestContext.csrfHelper.token
      val augmentedInfo = userInfo +
        ("csrfToken" -> JsString(token.map(_.value).getOrElse("Missing"))) +
        ("csrfHeader" -> JsString(requestContext.csrfHelper.headerName()))

      Ok(Json.obj(
        "refresh" -> (if (refresh) loginUrl else false),
        "user" -> augmentedInfo,
        "links" -> Json.obj(
          "login" -> links.getPermissionDeniedLink(request.context.user.nonEmpty),
          "logout" -> logoutUrl
        )
      )).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  def logout = Action { request =>
    val host = request.getQueryString("target").get
    val links = ssoClient.linkGenerator(request)
    links.setTarget(host)
    val redirect = Redirect(links.getLogoutUrl)
    redirect.discardingCookies(DiscardingCookie(SSC_NAME, SSC_PATH, Some(SSC_DOMAIN)))
  }

  private val WARWICK_ITS_CLASS = "warwickitsclass"
  private val WARWICK_YEAR_OF_STUDY = "warwickyearofstudy"
  private val WARWICK_FINAL_YEAR = "warwickfinalyear"

  private def contextUserInfo(context: LoginContext): Future[JsObject] = {
    context.user.map { user =>
      photoService.photoUrl(user.universityId)
        .recover { case _ => routes.Assets.versioned("images/no-photo.png").toString }
        .map { photo =>
          Json.obj(
            "authenticated" -> true,
            "usercode" -> user.usercode.string,
            "analytics" -> Json.obj(
              "identifier" -> measurementService.getUserIdentifier(user.usercode),
              "dimensions" -> Json.arr(
                Json.obj(
                  "index" -> 1,
                  "value" -> user.department.flatMap(_.shortName).getOrElse(null)
                ),
                Json.obj(
                  "index" -> 2,
                  "value" -> user.rawProperties.getOrElse(WARWICK_ITS_CLASS, null)
                ),
                Json.obj(
                  "index" -> 3,
                  "value" -> user.rawProperties.getOrElse(WARWICK_YEAR_OF_STUDY, null)
                ),
                Json.obj(
                  "index" -> 4,
                  "value" -> user.rawProperties.getOrElse(WARWICK_FINAL_YEAR, null)
                )
              )
            ),
            "name" -> user.name.full,
            "masquerading" -> context.isMasquerading,
            "photo" -> Json.obj(
              "url" -> photo
            )
          )
        }
    }.getOrElse(
      Future.successful(
        Json.obj(
          "authenticated" -> false
        )
      )
    )
  }

  private def hasValue(cookie: Cookie): Boolean =
    StringUtils.hasText(cookie.value)

  private def tokenNotInUserCache(cookie: Cookie): Boolean =
    userCache.get(new SSOToken(cookie.value, SSC_TICKET_TYPE)) == null

}
