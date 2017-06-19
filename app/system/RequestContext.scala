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

  def authenticated(sso: SSOClient, request: AuthenticatedRequest[_], navigation: Seq[Navigation], csrfHelper: CSRFPageHelper) =
    RequestContext(sso, request, request.context.user, request.context.actualUser, navigation, transformCsrfHelper(csrfHelper, request))

  def authenticated(sso: SSOClient, request: RequestHeader, csrfHelper: CSRFPageHelper): RequestContext = {
    val eventualRequestContext = sso.withUser(request) { loginContext =>
      Future.successful(Right(RequestContext(sso, request, loginContext.user, loginContext.actualUser, Nil, transformCsrfHelper(csrfHelper, request))))
    }.map(_.right.get)

    Await.result(eventualRequestContext, Duration.Inf)
  }

  def anonymous(sso: SSOClient, request: RequestHeader, navigation: Seq[Navigation], csrfHelper: CSRFPageHelper) = RequestContext(sso, request, None, None, navigation, csrfHelper)

  def apply(sso: SSOClient, request: RequestHeader, user: Option[User], actualUser: Option[User], navigation: Seq[Navigation], csrfHelper: CSRFPageHelper): RequestContext = {
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
      csrfHelper = transformCsrfHelper(csrfHelper, request)
    )
  }

  private[this] def transformCsrfHelper(helper: CSRFPageHelper, req: RequestHeader): CSRFPageHelper = {
    val token = play.filters.csrf.CSRF.getToken(req)
    helper.token = token
    helper
  }

}
