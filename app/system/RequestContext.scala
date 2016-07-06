package system

import play.api.mvc.RequestHeader
import warwick.sso.{AuthenticatedRequest, SSOClient, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class RequestContext(
  path: String,
  user: Option[User],
  actualUser: Option[User],
  loginUrl: String,
  logoutUrl: String
)

object RequestContext {

  def authenticated(sso: SSOClient, request: AuthenticatedRequest[_]) =
    RequestContext(sso, request, request.context.user, request.context.actualUser)

  def authenticated(sso: SSOClient, request: RequestHeader): RequestContext = {
    val eventualRequestContext = sso.withUser(request) { loginContext =>
      Future.successful(Right(RequestContext(sso, request, loginContext.user, loginContext.actualUser)))
    }.map(_.right.get)

    Await.result(eventualRequestContext, Duration.Inf)
  }

  def anonymous(sso: SSOClient, request: RequestHeader) =RequestContext(sso, request, None, None)

  def apply(sso: SSOClient, request: RequestHeader, user: Option[User], actualUser: Option[User]): RequestContext = {
    val linkGenerator = sso.linkGenerator(request)
    linkGenerator.setTarget(request.uri)

    RequestContext(
      path = request.path,
      user = user,
      actualUser = actualUser,
      loginUrl = linkGenerator.getLoginUrl,
      logoutUrl = linkGenerator.getLogoutUrl
    )
  }

}
