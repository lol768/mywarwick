package system

import com.google.inject.Inject
import play.api.mvc.Request
import services.NavigationService
import warwick.sso.{AuthenticatedRequest, LoginContext, SSOClient, User}

trait ImplicitRequestContext {

  @Inject
  val navigationService: NavigationService = null

  @Inject
  val ssoClient: SSOClient = null

  implicit def userProperties(implicit request: Request[_]): UserProperties = request match {
    case req: AuthenticatedRequest[_] => new UserProperties {
      override def user: Option[User] = req.context.user
      override def actualUser: Option[User] = req.context.actualUser
    }
    case _ => new UserProperties {
      override def user: Option[User] = None
      override def actualUser: Option[User] = None
    }
  }

  implicit def requestContext(implicit request: Request[_]): RequestContext = request match {
    case req: AuthenticatedRequest[_] =>
      val nav = navigationService.getNavigation(req.context)
      RequestContext.authenticated(ssoClient, req, nav)
    case _ =>
      // Assumes anonymous users have no navigation
      RequestContext.anonymous(ssoClient, request, Seq.empty)
  }

}

