package services

import javax.inject.Inject

import com.google.inject.{ImplementedBy, Provider}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import system.ImplicitRequestContext
import warwick.sso._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[SecurityServiceImpl])
trait SecurityService {
  def UserAction: ActionBuilder[AuthenticatedRequest, AnyContent]

  def RequiredUserAction: ActionBuilder[AuthenticatedRequest, AnyContent]

  def RequiredRoleAction(role: RoleName): ActionBuilder[AuthenticatedRequest, AnyContent]

  def RequiredActualUserRoleAction(role: RoleName): ActionBuilder[AuthenticatedRequest, AnyContent]

  def APIAction: ActionBuilder[AuthenticatedRequest, AnyContent]

  /**
    * An async result that will either do what you ask (A) or fall back to an error Result.
    * Used as a handler type for websockets.
    */
  type TryAccept[A] = Future[Either[Result, A]]

  def SecureWebsocket[A](request: play.api.mvc.RequestHeader)(block: warwick.sso.LoginContext => TryAccept[A]): TryAccept[A]

}

/**
  * Wraps up all the auth stuff from other libraries so that we can easily swap out
  * behaviours in controllers without them being tied into SSOClient specifics, and
  * we can combine actions together here too.
  */
class SecurityServiceImpl @Inject()(
  override val ssoClient: SSOClient,
  val basicAuth: BasicAuth,
  parse: PlayBodyParsers
) (implicit ec: ExecutionContext) extends SecurityService with ImplicitRequestContext {

  def defaultParser = parse.default

  override def UserAction = ssoClient.Lenient(defaultParser)
  override def RequiredUserAction = ssoClient.Strict(defaultParser)

  override def RequiredRoleAction(role: RoleName) = ssoClient.RequireRole(role, otherwise = showForbidden)(defaultParser)

  override def RequiredActualUserRoleAction(role: RoleName) = ssoClient.RequireActualUserRole(role, otherwise = showForbidden)(defaultParser)

  // TODO this always returns a forbidden result if no user found. We might want API calls for anonymous users.
  def APIAction: ActionBuilder[AuthenticatedRequest, AnyContent] =
    ssoClient.Lenient(defaultParser) andThen new BasicAuthFallback(defaultParser, ec)

  override def SecureWebsocket[A](request: play.api.mvc.RequestHeader)(block: warwick.sso.LoginContext => TryAccept[A]) =
    ssoClient.withUser(request)(block)

  /**
    * If a user already exists in the incoming AuthenticatedRequest, we use that.
    * otherwise we try Basic Auth.
    */
  class BasicAuthFallback[C](parser: BodyParser[C], val executionContext: ExecutionContext) extends ActionFunction[AuthenticatedRequest, AuthenticatedRequest] {
    override def invokeBlock[A](request: AuthenticatedRequest[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
      if (request.context.user.exists(_.isFound)) block(request)
      else basicAuth.Check(basicAuthDenied)(parser).invokeBlock(request, block)
    }
  }

  def basicAuthDenied(request: RequestHeader) = Future {
    Unauthorized(Json.obj(
      "success" -> false,
      "status" -> "unauthorized",
      "errors" -> Json.arr(
        Json.obj(
          "id" -> "unauthorized",
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

    Forbidden(views.html.errors.forbidden(identity)(requestContext(request)))
  }

}
