package system

import play.api.mvc.{Flash, Request, RequestHeader}
import play.filters.csrf.CSRF
import services.Navigation
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
  csrfHelper: CSRFPageHelper
)

object RequestContext {

  def authenticated(sso: SSOClient, request: AuthenticatedRequest[_], navigation: Seq[Navigation], csrfHelperFactory: CSRFPageHelperFactory) =
    RequestContext(sso, request, request.context.user, request.context.actualUser, navigation, csrfHelperFactory)

  def authenticated(sso: SSOClient, request: RequestHeader, csrfHelperFactory: CSRFPageHelperFactory): RequestContext = {
    val eventualRequestContext = sso.withUser(request) { loginContext =>
      Future.successful(Right(RequestContext(sso, request, loginContext.user, loginContext.actualUser, Nil, csrfHelperFactory)))
    }.map(_.right.get)

    Await.result(eventualRequestContext, Duration.Inf)
  }

  def anonymous(sso: SSOClient, request: RequestHeader, navigation: Seq[Navigation], csrfHelperFactory: CSRFPageHelperFactory) = RequestContext(sso, request, None, None, navigation, csrfHelperFactory)

  def apply(sso: SSOClient, request: RequestHeader, user: Option[User], actualUser: Option[User], navigation: Seq[Navigation], csrfPageHelperFactory: CSRFPageHelperFactory): RequestContext = {
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
      csrfHelper = transformCsrfHelper(csrfPageHelperFactory, request)
    )
  }

  private[this] def transformCsrfHelper(helperFactory: CSRFPageHelperFactory, req: RequestHeader): CSRFPageHelper = {
    val token = play.filters.csrf.CSRF.getToken(req)

    val helper = helperFactory.getInstance(token)
    helper
  }

}
