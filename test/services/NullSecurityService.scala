package services

import play.api.mvc.{RequestHeader, ActionBuilder}
import services.ActionBuilders.NullSecure
import warwick.sso.{RoleName, LoginContext, AuthenticatedRequest}

/**
  * Test SecurityService that doesn't do any checks - all actions allow the request through.
  */
class NullSecurityService(ctx: LoginContext) extends SecurityService {
  override def UserAction: ActionBuilder[AuthenticatedRequest] = new NullSecure(ctx)
  override def APIAction: ActionBuilder[AuthenticatedRequest] = new NullSecure(ctx)
  override def RequiredUserAction: ActionBuilder[AuthenticatedRequest] = new NullSecure(ctx)
  override def RequiredRoleAction(role: RoleName) = new NullSecure(ctx)
  override def RequiredActualUserRoleAction(role: RoleName) = new NullSecure(ctx)
  override def SecureWebsocket[A](request: RequestHeader)(block: (LoginContext) => TryAccept[A]): TryAccept[A] = block(ctx)
}
