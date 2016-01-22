package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import warwick.sso._

import scala.concurrent.Future

@ImplementedBy(classOf[SecurityServiceImpl])
trait SecurityService {

  def UserAction: ActionBuilder[AuthenticatedRequest]

  def RequiredUserAction: ActionBuilder[AuthenticatedRequest]

  def RequiredRoleAction(role: RoleName): ActionBuilder[AuthenticatedRequest]

  def RequiredActualUserRoleAction(role: RoleName): ActionBuilder[AuthenticatedRequest]

  def APIAction: ActionBuilder[AuthenticatedRequest]

  type TryAccept[A] = Future[Either[Result, A]]

  def SecureWebsocket[A](request: play.api.mvc.RequestHeader)(block: warwick.sso.LoginContext => TryAccept[A]): TryAccept[A]

}

/**
  * Wraps up all the auth stuff from other libraries so that we can easily swap out
  * behaviours in controllers without them being tied into SSOClient specifics, and
  * we can combine actions together here too.
  */
class SecurityServiceImpl @Inject()(
  val ssoClient: SSOClient,
  val basicAuth: BasicAuth,
  cache: CacheApi
) extends SecurityService {

  import play.api.libs.concurrent.Execution.Implicits._

  val UserAction = ssoClient.Lenient
  val RequiredUserAction = ssoClient.Strict

  def RequiredRoleAction(role: RoleName) = ssoClient.RequireRole(role, otherwise = showForbidden)

  def RequiredActualUserRoleAction(role: RoleName) = ssoClient.RequireActualUserRole(role, otherwise = showForbidden)

  // TODO this always returns a forbidden result if no user found. We might want API calls for anonymous users.
  val APIAction = ssoClient.Lenient andThen BasicAuthFallback

  override def SecureWebsocket[A](request: play.api.mvc.RequestHeader)(block: warwick.sso.LoginContext => TryAccept[A]) =
    ssoClient.withUser(request)(block)

  /**
    * If a user already exists in the incoming AuthenticatedRequest, we use that.
    * otherwise we try Basic Auth.
    */
  object BasicAuthFallback extends ActionFunction[AuthenticatedRequest, AuthenticatedRequest] {
    override def invokeBlock[A](request: AuthenticatedRequest[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
      if (request.context.user.exists(_.isFound)) block(request)
      else basicAuth.Check(basicAuthDenied).invokeBlock(request, block)
    }
  }

  def basicAuthDenied(request: RequestHeader) = Future {
    Unauthorized(Json.obj(
      "success" -> true,
      "status" -> "unauthorized",
      "errors" -> Json.arr(
        Json.obj(
          "message" -> "Authentication failed.  You may authenticate through Single Sign-On or HTTP Basic authentication."
        )
      )
    ))
  }

  def showForbidden(request: AuthenticatedRequest[_]) = {
    import request.context._
    val identity = for {
      name <- user.flatMap(_.name.first)
      actualName <- actualUser.flatMap(_.name.first)
    } yield {
      if (isMasquerading)
        s"$name (really $actualName)"
      else
        name
    }

    Forbidden(views.html.errors.forbidden(identity))
  }

}
