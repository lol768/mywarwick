package system

import play.api.mvc.{Flash, Request, RequestHeader}
import play.filters.csrf.CSRF
import services.{Features, FeaturesService, Navigation}
import warwick.sso.{AuthenticatedRequest, SSOClient, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class RequestContext(
  path: String,
  user: Option[User],
  actualUser: Option[User],
  loginUrl: String,
  logoutUrl: String,
  navigation: Seq[Navigation],
  flash: Flash,
  csrfHelper: CSRFPageHelper,
  features: Features // User's specific features, depending on their EAP preferences
)

object RequestContext {

  def authenticated(sso: SSOClient, request: AuthenticatedRequest[_], navigation: Seq[Navigation], csrfHelperFactory: CSRFPageHelperFactory, features: Features) =
    RequestContext(sso, request, request.context.user, request.context.actualUser, navigation, csrfHelperFactory, features)

  def authenticated(sso: SSOClient, request: RequestHeader, csrfHelperFactory: CSRFPageHelperFactory, features: FeaturesService): RequestContext = {
    val eventualRequestContext = sso.withUser(request) { loginContext =>
      Future.successful(Right(RequestContext(sso, request, loginContext.user, loginContext.actualUser, Nil, csrfHelperFactory, features.get(loginContext.user.map(_.usercode)))))
    }.map(_.right.get)

    Await.result(eventualRequestContext, Duration.Inf)
  }

  def anonymous(sso: SSOClient, request: RequestHeader, navigation: Seq[Navigation], csrfHelperFactory: CSRFPageHelperFactory, features: Features) = RequestContext(sso, request, None, None, navigation, csrfHelperFactory, features)

  def apply(sso: SSOClient, request: RequestHeader, user: Option[User], actualUser: Option[User], navigation: Seq[Navigation], csrfPageHelperFactory: CSRFPageHelperFactory, features: Features): RequestContext = {
    val target = (if (request.secure) "https://" else "http://") + request.host + request.uri
    val linkGenerator = sso.linkGenerator(request)
    linkGenerator.setTarget(target)

    RequestContext(
      path = request.path,
      user = user,
      actualUser = actualUser,
      loginUrl = linkGenerator.getLoginUrl,
      logoutUrl = linkGenerator.getLogoutUrl,
      navigation = navigation,
      flash = request.flash,
      csrfHelper = transformCsrfHelper(csrfPageHelperFactory, request),
      features = features
    )
  }

  private[this] def transformCsrfHelper(helperFactory: CSRFPageHelperFactory, req: RequestHeader): CSRFPageHelper = {
    val token = play.filters.csrf.CSRF.getToken(req)

    val helper = helperFactory.getInstance(token)
    helper
  }

}
