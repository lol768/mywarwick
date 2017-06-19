package system

import com.google.inject.Inject
import play.api.mvc.Request
import services.NavigationService
import warwick.sso.{AuthenticatedRequest, SSOClient}

trait ImplicitRequestContext {

  @Inject
  val navigationService: NavigationService = null

  @Inject
  val ssoClient: SSOClient = null

  @Inject
  val csrfHelper: CSRFPageHelper = null

  implicit def requestContext(implicit request: Request[_]): RequestContext = request match {
    case req: AuthenticatedRequest[_] =>
      val nav = navigationService.getNavigation(req.context)
      RequestContext.authenticated(ssoClient, req, nav, csrfHelper)
    case _ =>
      // Assumes anonymous users have no navigation
      RequestContext.anonymous(ssoClient, request, Seq.empty, csrfHelper)
  }

}